//overwrite
package org.devocative.ares.iservice;

import org.devocative.ares.entity.OServer;
import org.devocative.ares.vo.filter.OServerFVO;
import org.devocative.demeter.entity.User;

import java.util.List;

public interface IOServerService {
	void saveOrUpdate(OServer entity);

	OServer load(Long id);

	OServer loadByName(String name);

	OServer loadByAddress(String address);

	List<OServer> list();

	List<OServer> search(OServerFVO filter, long pageIndex, long pageSize);

	long count(OServerFVO filter);

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================
}