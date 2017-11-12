package org.devocative.ares.test;

import org.devocative.ares.cmd.ConsoleResultProcessing;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestConsoleResultProcessing {
	@Test
	public void testESXi() throws Exception {
		// the list generated by "vim-cmd vmsvc/getallvms" command
		byte[] bytes = Files.readAllBytes(Paths.get(getClass().getResource("/esxiListVM.txt").toURI()));
		String text = new String(bytes);

		ConsoleResultProcessing crp = new ConsoleResultProcessing(text);
		crp.setSplitBy("\\s{3,}");
		crp.build();

		Assert.assertEquals("[Vmid, Name, File, Guest OS, Version, Annotation]", crp.getColumns().toString());
		Assert.assertEquals(35, crp.getRows().size());
	}

	@Test
	public void testOSQ() throws Exception {
		byte[] bytes = Files.readAllBytes(Paths.get(getClass().getResource("/osqCSV.txt").toURI()));
		String text = new String(bytes);

		ConsoleResultProcessing crp = new ConsoleResultProcessing(text);
		crp.setSplitBy("[|]");
		crp.build();

//		Assert.assertEquals("[Vmid, Name, File, Guest OS, Version, Annotation]", crp.getColumns().toString());
//		Assert.assertEquals(35, crp.getRows().size());
	}

	@Test
	public void testLinPS() throws Exception {
		byte[] bytes = Files.readAllBytes(Paths.get(getClass().getResource("/ps.txt").toURI()));
		String text = new String(bytes);

		ConsoleResultProcessing crp = new ConsoleResultProcessing(text);
		crp.setPossibleColumns("PID", "TTY", "STAT", "COMMAND", "ALAKI", "TIME");
		crp.build();

		Assert.assertEquals("[PID, TTY, STAT, TIME, COMMAND]",
			crp.getColumns().toString());
		Assert.assertEquals(131, crp.getRows().size());
	}

	@Test
	public void testLinNetstat() throws Exception {
		byte[] bytes = Files.readAllBytes(Paths.get(getClass().getResource("/netstat.txt").toURI()));
		String text = new String(bytes);

		ConsoleResultProcessing crp = new ConsoleResultProcessing(text);
		crp
			.setIgnoreStartingLines(1)
			.setPossibleColumns("Proto", "Recv-Q", "Send-Q", "PID/Program name", "Local Address", "ALAKI", "Foreign Address", "State");
		crp.build();

		Assert.assertEquals("[Proto, Recv-Q, Send-Q, Local Address, Foreign Address, State, PID/Program name]",
			crp.getColumns().toString());
		Assert.assertEquals(14, crp.getRows().size());
	}

	@Test
	public void testWinPort() throws Exception {
		byte[] bytes = Files.readAllBytes(Paths.get(getClass().getResource("/winPortForward.txt").toURI()));
		String text = new String(bytes);

		ConsoleResultProcessing crp = new ConsoleResultProcessing(text);
		crp
			.setIgnoreStartingLines(3)
			.setIgnoreLinesAfterHeader(1)
			.setPossibleColumns("Address", "Port", "Local Address", "ALAKI", "Foreign Address", "State");
		crp.build();

		Assert.assertEquals("[Address, Port, Address, Port]",
			crp.getColumns().toString());
		Assert.assertEquals(19, crp.getRows().size());
	}

	@Test
	public void testLsOf() throws Exception {
		byte[] bytes = Files.readAllBytes(Paths.get(getClass().getResource("/lsof.txt").toURI()));
		String text = new String(bytes);

		ConsoleResultProcessing crp = new ConsoleResultProcessing(text);
		crp.setPossibleColumns("COMMAND", "PID", "TID", "USER", "FD", "TYPE", "DEVICE", "SIZE/OFF", "NODE", "NAME");
		crp.build();

		Assert.assertEquals("[COMMAND, PID, USER, FD, TYPE, DEVICE, SIZE/OFF, NODE, NAME]", crp.getColumns().toString());
		Assert.assertEquals(30, crp.getRows().size());

		/*List<String> row1 = crp.getRows().get(0);
		Assert.assertEquals("systemd", row1.get(0));
		Assert.assertEquals("1", row1.get(1));
		Assert.assertEquals("root", row1.get(2));
		Assert.assertEquals("cwd", row1.get(3));
		Assert.assertEquals("DIR", row1.get(4));
		Assert.assertEquals("251,0", row1.get(5));
		Assert.assertEquals("4096", row1.get(6));
		Assert.assertEquals("128", row1.get(7));
		Assert.assertEquals("/", row1.get(8));*/
	}
}
