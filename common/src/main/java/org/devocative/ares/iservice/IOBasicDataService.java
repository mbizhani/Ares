//overwrite
package org.devocative.ares.iservice;

import org.devocative.ares.entity.OBasicData;
import org.devocative.ares.vo.filter.OBasicDataFVO;
import org.devocative.demeter.entity.User;

import java.util.List;

public interface IOBasicDataService {
	void saveOrUpdate(OBasicData entity);

	OBasicData load(Long id);

	List<OBasicData> list();

	List<OBasicData> search(OBasicDataFVO filter, long pageIndex, long pageSize);

	long count(OBasicDataFVO filter);

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================
}