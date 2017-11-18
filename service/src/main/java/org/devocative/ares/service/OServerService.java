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
	public KeyValueVO<String, String> findGuestOf(Long hypervisorId, String vmId) {
		OServer oServer = persistorService.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisorId = :hypervisorId")
			.addWhere("and ent.vmId = :vmId")
			.addParam("hypervisorId", hypervisorId)
			.addParam("vmId", vmId)
			.object();

		if (oServer != null) {
			return new KeyValueVO<>(oServer.getVmId(), oServer.getName());
		}

		return null;
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
	public KeyValueVO<Long, String> findServerAsVM(Long id) {
		OServer oServer = persistorService.createQueryBuilder()
			.addFrom(OServer.class, "ent")
			.addWhere("and ent.hypervisor is not null")
			.addWhere("and ent.id = :id")
			.addParam("id", id)
			.object();

		return new KeyValueVO<>(oServer.getId(), oServer.getName());
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
		logger.info("CheckVMServers: hypervisor=[{}] servers={}", hypervisorId, servers);

		persistorService.createQueryBuilder()
			.addSelect("update OServer ent set ent.vmId = null where ent.hypervisor.id = :hypervisorId")
			.addParam("hypervisorId", hypervisorId)
			.update();

		for (Map<String, String> server : servers) {
			String name = server.get("name");
			String address = server.get("address");
			String vmId = server.get("vmId");
			String os = server.get("os");

			OServer oServer = persistorService.createQueryBuilder()
				.addFrom(OServer.class, "ent")
				.addWhere("and ent.name = :name")
				.addParam("name", name)
				.addWhere("and ent.hypervisor.id = :hypervisorId")
				.addParam("hypervisorId", hypervisorId)
				.object();

			if (oServer != null) {
				logger.info("CheckVMServers: update server id=[{}] name=[{}]", oServer.getId(), oServer.getName());
				oServer.setVmId(vmId);
				if (address != null) {
					oServer.setAddress(address);
				}
			} else {
				logger.info("CheckVMServers: insert new server name=[{}] vmId=[{}]", name, vmId);
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
		}

		persistorService.commitOrRollback();
	}

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
			persistorService.commitOrRollback();
		} else {
			throw new RuntimeException(String.format("UpdateServer: VM not found hypervisorId=[%s] vmId=[%s]", hypervisorId, oldVmId));
		}
	}
}