package org.devocative.ares.entity.oservice;

import org.devocative.ares.entity.OServer;
import org.devocative.demeter.entity.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Audited
@Entity
@Table(name = "t_ars_service_inst", uniqueConstraints = {
	@UniqueConstraint(name = "uk_ars_serviceInst", columnNames = {"f_server", "f_service", "c_name"})
})
public class OServiceInstance implements ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = 2007755808784442971L;

	@Id
	@GeneratedValue(generator = "ars_service_inst")
	@org.hibernate.annotations.GenericGenerator(name = "ars_service_inst", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ars_service_inst")
		})
	private Long id;

	@Column(name = "c_name")
	private String name;

	@Column(name = "n_port")
	private Integer port;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_server", nullable = false, foreignKey = @ForeignKey(name = "srvcinst2server"))
	private OServer server;

	@Column(name = "f_server", insertable = false, updatable = false)
	private Long serverId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_service", nullable = false, foreignKey = @ForeignKey(name = "srvcinst2service"))
	private OService service;

	@Column(name = "f_service", nullable = false, insertable = false, updatable = false)
	private Long serviceId;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "serviceInstance", cascade = CascadeType.ALL)
	private List<OSIPropertyValue> propertyValues;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_ars_srvcinst_user",
		joinColumns = {@JoinColumn(name = "f_srvcinst", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_user", nullable = false)},
		foreignKey = @ForeignKey(name = "srvcinstUser2srvcinst"),
		inverseForeignKey = @ForeignKey(name = "srvcinstUser2user")
	)
	private List<User> allowedUsers;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_ars_srvcinst_role",
		joinColumns = {@JoinColumn(name = "f_srvcinst", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_role", nullable = false)},
		foreignKey = @ForeignKey(name = "srvcinstRole2srvcinst"),
		inverseForeignKey = @ForeignKey(name = "srvcinstRole2role")
	)
	private List<Role> allowedRoles;

	// --------------- CREATE / MODIFY

	@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", nullable = false, insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "srvcinst_crtrusr2user"))
	private User creatorUser;

	@NotAudited
	@Column(name = "f_creator_user", nullable = false)
	private Long creatorUserId;

	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "srvcinst_mdfrusr2user"))
	private User modifierUser;

	@Column(name = "f_modifier_user")
	private Long modifierUserId;

	@Version
	@Column(name = "n_version", nullable = false)
	private Integer version = 0;

	// ------------------------------

	public OServiceInstance() {
	}

	public OServiceInstance(Integer port, OServer server, OService service) {
		this.port = port;
		this.server = server;
		this.service = service;
	}

	// ------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getPortSafely() {
		return getPort() != null ? getPort() : getService().getAdminPort();
	}

	public OServer getServer() {
		return server;
	}

	public void setServer(OServer server) {
		this.server = server;
	}

	public Long getServerId() {
		return serverId;
	}

	public OService getService() {
		return service;
	}

	public void setService(OService service) {
		this.service = service;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public List<OSIPropertyValue> getPropertyValues() {
		return propertyValues;
	}

	public void setPropertyValues(List<OSIPropertyValue> propertyValues) {
		this.propertyValues = propertyValues;
	}

	public List<User> getAllowedUsers() {
		return allowedUsers;
	}

	public void setAllowedUsers(List<User> allowedUsers) {
		this.allowedUsers = allowedUsers;
	}

	public List<Role> getAllowedRoles() {
		return allowedRoles;
	}

	public void setAllowedRoles(List<Role> allowedRoles) {
		this.allowedRoles = allowedRoles;
	}

	// --------------- CREATE / MODIFY

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public User getCreatorUser() {
		return creatorUser;
	}

	@Override
	public Long getCreatorUserId() {
		return creatorUserId;
	}

	@Override
	public void setCreatorUserId(Long creatorUserId) {
		this.creatorUserId = creatorUserId;
	}

	@Override
	public Date getModificationDate() {
		return modificationDate;
	}

	@Override
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public User getModifierUser() {
		return modifierUser;
	}

	@Override
	public Long getModifierUserId() {
		return modifierUserId;
	}

	@Override
	public void setModifierUserId(Long modifierUserId) {
		this.modifierUserId = modifierUserId;
	}

	@Override
	public Integer getVersion() {
		return version;
	}

	@Override
	public void setVersion(Integer version) {
		this.version = version;
	}

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OServiceInstance)) return false;

		OServiceInstance that = (OServiceInstance) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return getName() == null ?
			String.format("%s(%s)", getServer(), getService()) :
			String.format("%s@%s(%s)", getName(), getServer(), getService());
	}
}
