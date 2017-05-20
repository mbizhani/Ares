package org.devocative.ares.service;

import org.devocative.adroit.vo.KeyValueVO;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.iservice.IOServerService;
import org.devocative.ares.vo.filter.OServerFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("arsOServerService")
public class OServerService implements IOServerService {
	private static final Logger logger = LoggerFactory.getLogger(OServerService.class);

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OServer entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public OServer load(Long id) {
		return persistorService.get(OServer.class, id);
	}

	@Override
	public OServer loadByName(String name) {
		return persistorService
			.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.name = :name")
			.addParam("name", name)
			.object();
	}

	@Override
	public OServer loadByAddress(String address) {
		return persistorService
			.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.address = :address")
			.addParam("address", address)
			.object();
	}

	@Override
	public List<OServer> list() {
		return persistorService.list(OServer.class);
	}

	@Override
	public List<OServer> search(OServerFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(OServer.class, "ent")
			.applyFilter(OServer.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(OServerFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(OServer.class, "ent")
			.applyFilter(OServer.class, "ent", filter)
			.object();
	}

	@Override
	public List<OServer> getHypervisorList() {
		return persistorService.list(OServer.class);
	}

	@Override
	public List<User> getOwnerList() {
		return persistorService.list(User.class);
	}

	@Override
	public List<User> getCreatorUserList() {
		return persistorService.list(User.class);
	}

	@Override
	public List<User> getModifierUserList() {
		return persistorService.list(User.class);
	}

	// ==============================

	@Override
	public List<KeyValueVO<String, String>> findGuestsOf(Long hypervisorId) {
		List<OServer> servers = persistorService.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisor.id = :hypervisorId")
			.addParam("hypervisorId", hypervisorId)
			.list();

		List<KeyValueVO<String, String>> result = new ArrayList<>();
		for (OServer oServer : servers) {
			result.add(new KeyValueVO<>(oServer.getVmId(), oServer.getName()));
		}
		return result;
	}

	@Override
	public List<KeyValueVO<Long, String>> findServersAsVM() {
		List<OServer> servers = persistorService.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisor is not null")
			.list();

		List<KeyValueVO<Long, String>> result = new ArrayList<>();
		for (OServer oServer : servers) {
			result.add(new KeyValueVO<>(oServer.getId(), oServer.getName()));
		}
		return result;
	}

	@Override
	public void updateVmid(Long id, String vmId) {
		persistorService.createQueryBuilder()
			.addSelect("update OServer ent set ent.vmId = :vmId where ent.id = :id")
			.addParam("vmId", vmId)
			.addParam("id", id)
			.update();
		persistorService.commitOrRollback();
	}

	@Override
	public void checkVMServers(Long hypervisorId, List<Map<String, String>> servers) {
		logger.info("checkVMServers: hypervisor=[{}] servers={}", hypervisorId, servers);

		persistorService.createQueryBuilder()
			.addSelect("update OServer ent set ent.vmId = null where ent.hypervisor.id = :hypervisorId")
			.addParam("hypervisorId", hypervisorId)
			.update();

		for (Map<String, String> server : servers) {
			String name = server.get("name");
			String address = server.get("address");
			String vmId = server.get("vmId");

			OServer oServer = persistorService.createQueryBuilder()
				.addFrom(OServer.class, "ent")
				.addWhere("and ent.name = :name")
				.addParam("name", name)
				.addWhere("and ent.hypervisor.id = :hypervisorId")
				.addParam("hypervisorId", hypervisorId)
				.object();

			if (oServer != null) {
				logger.info("checkVMServers: update server id=[{}] name=[{}]", oServer.getId(), oServer.getName());
				oServer.setVmId(vmId);
				if (address != null) {
					oServer.setAddress(address);
				}
			} else {
				logger.info("checkVMServers: insert new server name=[{}] vmId=[{}]", name, vmId);
				oServer = new OServer(name, address);
				oServer.setVmId(vmId);
				oServer.setHypervisor(new OServer(hypervisorId));
			}

			saveOrUpdate(oServer);
		}

		persistorService.commitOrRollback();
	}
}