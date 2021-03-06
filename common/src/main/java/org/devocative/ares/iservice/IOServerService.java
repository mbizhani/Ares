package org.devocative.ares.iservice;

import org.devocative.adroit.vo.KeyValueVO;
import org.devocative.ares.entity.OBasicData;
import org.devocative.ares.entity.OServer;
import org.devocative.ares.vo.filter.OServerFVO;
import org.devocative.demeter.entity.User;

import java.util.List;
import java.util.Map;

public interface IOServerService {
	void saveOrUpdate(OServer entity);

	OServer load(Long id);

	OServer loadByName(String name);

	OServer loadByAddress(String address);

	List<OServer> list();

	List<OServer> search(OServerFVO filter, long pageIndex, long pageSize);

	long count(OServerFVO filter);

	List<OBasicData> getFunctionList();

	List<OBasicData> getEnvironmentList();

	List<OBasicData> getLocationList();

	List<OBasicData> getCompanyList();

	List<OServer> getHypervisorList();

	List<User> getOwnerList();

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	List<KeyValueVO<String, String>> findGuestsOf(Long hypervisorId);

	KeyValueVO<String, String> findGuestOf(Long hypervisorId, String vmId);

	List<KeyValueVO<Long, String>> findServersAsVM();

	KeyValueVO<Long, String> findServerAsVM(Long id);

	void updateVmid(Long id, String vmId);

	List<String> updateVMServers(Long hypervisorId, String multiMatchAlg, List<Map<String, String>> servers, boolean onlyNew);

	List<OServer> checkVMServer(Long hypervisorId, String multiMatchAlg, String name, String vmId, String address);

	void updateServer(Long hypervisorId, String oldVmId, String newVmId, String newName);
}