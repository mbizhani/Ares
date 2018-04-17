package org.devocative.ares.service;

import org.devocative.adroit.vo.KeyValueVO;
import org.devocative.ares.entity.EBasicDiscriminator;
import org.devocative.ares.entity.EServerOS;
import org.devocative.ares.entity.OBasicData;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.ares.iservice.IOBasicDataService;
import org.devocative.ares.iservice.IOServerService;
import org.devocative.ares.iservice.oservice.IOServiceInstanceService;
import org.devocative.ares.iservice.oservice.IOServiceService;
import org.devocative.ares.vo.filter.OServerFVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service("arsOServerService")
public class OServerService implements IOServerService {
	private static final Logger logger = LoggerFactory.getLogger(OServerService.class);

	private static final String VM_ID = "Vmid";
	private static final String NAME = "Name";
	private static final String ADDRESS = "Address";

	// ------------------------------

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IOBasicDataService basicDataService;

	@Autowired
	private IOServiceService oServiceService;

	@Autowired
	private IOServiceInstanceService oServiceInstanceService;

	// ------------------------------

	@Override
	public void saveOrUpdate(OServer entity) {
		persistorService.saveOrUpdate(entity);

		if (entity.getServerOS() != null) {
			OService oService = oServiceService.loadByName(entity.getServerOS().getName());
			if (oService != null) {
				OServiceInstance serviceInstance = oServiceInstanceService.loadByServerAndService(entity, oService);
				if (serviceInstance == null) {
					oServiceInstanceService.saveOrUpdate(new OServiceInstance(null, entity, oService));
				}
			}
		}
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
			.setOrderBy("ent.hypervisorId nulls first")
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
	public List<OBasicData> getFunctionList() {
		return basicDataService.listByDiscriminator(EBasicDiscriminator.FUNCTION);
	}

	@Override
	public List<OBasicData> getEnvironmentList() {
		return basicDataService.listByDiscriminator(EBasicDiscriminator.ENVIRONMENT);
	}

	@Override
	public List<OBasicData> getLocationList() {
		return basicDataService.listByDiscriminator(EBasicDiscriminator.LOCATION);
	}

	@Override
	public List<OBasicData> getCompanyList() {
		return basicDataService.listByDiscriminator(EBasicDiscriminator.COMPANY);
	}

	@Override
	public List<OServer> getHypervisorList() {
		return persistorService.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisor.id is null")
			.list();
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
		List<Object[]> servers = persistorService.createQueryBuilder()
			.addSelect("select ent.vmId, ent.name")
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisor.id = :hypervisorId")
			.addParam("hypervisorId", hypervisorId)
			.list();

		List<KeyValueVO<String, String>> result = new ArrayList<>();
		for (Object[] oServer : servers) {
			result.add(new KeyValueVO<>((String) oServer[0], (String) oServer[1]));
		}
		return result;
	}

	@Override
	public KeyValueVO<String, String> findGuestOf(Long hypervisorId, String vmId) {
		Object[] oServer = persistorService.createQueryBuilder()
			.addSelect("select ent.vmId, ent.name")
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisorId = :hypervisorId")
			.addWhere("and ent.vmId = :vmId")
			.addParam("hypervisorId", hypervisorId)
			.addParam("vmId", vmId)
			.object();

		if (oServer != null) {
			return new KeyValueVO<>((String) oServer[0], (String) oServer[1]);
		}

		return null;
	}

	@Override
	public List<KeyValueVO<Long, String>> findServersAsVM() {
		List<Object[]> servers = persistorService.createQueryBuilder()
			.addSelect("select ent.id, ent.name")
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisor is not null")
			.list();

		List<KeyValueVO<Long, String>> result = new ArrayList<>();
		for (Object[] oServer : servers) {
			result.add(new KeyValueVO<>((Long) oServer[0], (String) oServer[1]));
		}
		return result;
	}

	@Override
	public KeyValueVO<Long, String> findServerAsVM(Long id) {
		Object[] oServer = persistorService.createQueryBuilder()
			.addSelect("select ent.id, ent.name")
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisor is not null")
			.addWhere("and ent.id = :id")
			.addParam("id", id)
			.object();

		return new KeyValueVO<>((Long) oServer[0], (String) oServer[1]);
	}

	@Transactional
	@Override
	public void updateVmid(Long id, String vmId) {
		persistorService.createQueryBuilder()
			.addSelect("update OServer ent set ent.vmId = :vmId where ent.id = :id")
			.addParam("vmId", vmId)
			.addParam("id", id)
			.update();
		//TODO persistorService.commitOrRollback();
	}

	@Transactional
	@Override
	public List<String> updateVMServers(Long hypervisorId, String multiMatchAlg, List<Map<String, String>> servers, boolean onlyNew) {
		logger.info("UpdateVMServers: hypervisor=[{}] multiMatchAlg=[{}] servers={}", hypervisorId, multiMatchAlg, servers);

		switch (multiMatchAlg) {
			case VM_ID:
				assertNoConflict("vmId", servers);
				break;

			case NAME:
				assertNoConflict("name", servers);
				break;

			case ADDRESS:
				assertNoConflict("address", servers);
				break;
		}

		List<String> result = new ArrayList<>();
		List<String> validVmId = new ArrayList<>();

		for (Map<String, String> server : servers) {
			String name = server.get("name").trim();
			String vmId = server.get("vmId").trim();
			String address = server.get("address") != null ? server.get("address").trim() : null;
			String os = server.get("os") != null ? server.get("os").trim() : null;

			validVmId.add(vmId);

			List<OServer> oServers = checkVMServer(hypervisorId, multiMatchAlg, name, vmId, address);

			OServer oServer = null;
			if (oServers.size() == 1) {
				oServer = oServers.get(0);
			} else if (oServers.size() > 1) {
				throw new RuntimeException("Multiple Server Found: " + oServers);
			}

			if (onlyNew && oServer != null) {
				continue;
			}

			if (oServer != null) {
				logger.info("UpdateVMServers for Hypervisor=[{}]: vmId=[{}] name=[{}] addr=[{}] <- OServer(id=[{}] name=[{}] addr=[{}])",
					hypervisorId,
					vmId, name, address,
					oServer.getId(), oServer.getName(), oServer.getAddress()
				);

				oServer.setVmId(vmId);
				oServer.setName(name);
				if (address != null && !address.isEmpty()) {
					oServer.setAddress(address);
				}
			} else {
				logger.info("UpdateVMServers for Hypervisor=[{}]: insert new server vmId=[{}] name=[{}]",
					hypervisorId, name, vmId);
				oServer = new OServer(name, address);
				oServer.setVmId(vmId);
				oServer.setHypervisor(new OServer(hypervisorId));
			}

			if (os != null) {
				switch (os) {
					case "Linux":
						oServer.setServerOS(EServerOS.LINUX);
						break;
					case "Windows":
						oServer.setServerOS(EServerOS.WINDOWS);
						break;
				}
			}

			saveOrUpdate(oServer);
			result.add(oServer.toString());
		}

		int noOfInvalidVmId = persistorService.createQueryBuilder()
			.addSelect("update OServer ent set ent.vmId = null where ent.hypervisor.id = :hypervisorId and ent.vmId not in (:validVmId)")
			.addParam("hypervisorId", hypervisorId)
			.addParam("validVmId", validVmId)
			.update();

		logger.info("Update hypervisor's VM: hypervisorId=[{}] no of invalid vmId = [{}]", hypervisorId, noOfInvalidVmId);

		//TODO persistorService.commitOrRollback();

		return result;
	}

	@Override
	public List<OServer> checkVMServer(Long hypervisorId, String multiMatchAlg, String name, String vmId, String address) {
		logger.info("CheckVMServer: hypervisor=[{}] multiMatchAlg=[{}] name=[{}] vmId=[{}] address=[{}]",
			hypervisorId, multiMatchAlg, name, vmId, address);

		IQueryBuilder oServerBuilder = persistorService.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisor.id = :hypervisorId")
			.addParam("hypervisorId", hypervisorId);

		if (multiMatchAlg == null) {
			oServerBuilder
				.addWhere("and (ent.name = :name")
				.addParam("name", name)
				.addWhere("or ent.vmId = :vmId")
				.addParam("vmId", vmId);
			if (address != null && !address.trim().isEmpty()) {
				oServerBuilder
					.addWhere("or ent.address = :address")
					.addParam("address", address.trim());
			}
		} else {
			oServerBuilder.addWhere("and (1=0");
			switch (multiMatchAlg) {
				case VM_ID:
					oServerBuilder
						.addWhere("or ent.vmId = :vmId")
						.addParam("vmId", vmId);
					break;

				case NAME:
					oServerBuilder
						.addWhere("or ent.name = :name")
						.addParam("name", name);
					break;

				case ADDRESS:
					if (address != null && !address.trim().isEmpty()) {
						oServerBuilder
							.addWhere("or ent.address = :address")
							.addParam("address", address.trim());
					}
					break;
			}
		}

		oServerBuilder.addWhere(")");

		List<OServer> oServerList = oServerBuilder.list();

		if (oServerList.isEmpty() || oServerList.size() == 1) {
			return oServerList;
		} else {
			return findProperOServer(oServerList, multiMatchAlg, vmId, name, address);
		}
	}

	@Transactional
	@Override
	public void updateServer(Long hypervisorId, String oldVmId, String newVmId, String newName) {
		OServer oServer = persistorService.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisor.id = :hypervisorId")
			.addParam("hypervisorId", hypervisorId)
			.addWhere("and ent.vmId = :vmId")
			.addParam("vmId", oldVmId)
			.object();

		if (oServer != null) {
			oServer.setName(newName);
			oServer.setVmId(newVmId);
			saveOrUpdate(oServer);
			//TODO persistorService.commitOrRollback();
		} else {
			throw new RuntimeException(String.format("UpdateServer: VM not found hypervisorId=[%s] vmId=[%s]", hypervisorId, oldVmId));
		}
	}

	// ------------------------------

	private List<OServer> findProperOServer(List<OServer> oServerList, String multiMatchAlg, String vmId, String name, String address) {
		if (multiMatchAlg == null) {
			return oServerList;
		}

		switch (multiMatchAlg) {
			case VM_ID:
				for (OServer oServer : oServerList) {
					if (oServer.getVmId().equals(vmId)) {
						return Collections.singletonList(oServer);
					}
				}
				break;

			case NAME:
				for (OServer oServer : oServerList) {
					if (oServer.getName().equals(name)) {
						return Collections.singletonList(oServer);
					}
				}
				break;

			case ADDRESS:
				if (address != null) {
					for (OServer oServer : oServerList) {
						if (oServer.getAddress() != null && address.equals(oServer.getAddress())) {
							return Collections.singletonList(oServer);
						}
					}
				}
				break;

			default:
				throw new RuntimeException("Invalid match algorithm: " + multiMatchAlg);
		}

		return oServerList;
	}

	private void assertNoConflict(String field, List<Map<String, String>> servers) {
		Set<String> set = new HashSet<>();
		for (Map<String, String> server : servers) {
			String value = server.get(field);
			if (value == null || value.trim().isEmpty()) {
				throw new RuntimeException(String.format("No value for '%s': %s", field, server)); //TODO
			}

			value = value.trim();

			if (set.contains(value)) {
				throw new RuntimeException(String.format("Duplicate '%s': %s [%s]", field, value, server)); //TODO
			} else {
				set.add(value);
			}
		}
	}
}