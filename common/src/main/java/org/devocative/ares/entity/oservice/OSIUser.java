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
@Table(name = "t_ars_service_inst_user", uniqueConstraints = {
	@UniqueConstraint(name = "uk_ars_user_username", columnNames = {"c_username", "f_service_inst"})
})
public class OSIUser implements IRowMod, ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = 753142909119873415L;

	@Id
	@GeneratedValue(generator = "ars_service_inst_user")
	@org.hibernate.annotations.GenericGenerator(name = "ars_service_inst_user", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ars_service_inst_user")
		})
	private Long id;

	@Column(name = "c_username", nullable = false)
	private String username;

	@Column(name = "c_password", nullable = false)
	private String password;

	@Column(name = "b_executor", nullable = false)
	private Boolean executor;

	@Column(name = "b_enabled", nullable = false)
	private Boolean enabled;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_remote_mode", nullable = false))
	private ERemoteMode remoteMode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_service_inst", nullable = false, foreignKey = @ForeignKey(name = "siUser2serviceInstance"))
	private OServiceInstance serviceInstance;

	@Column(name = "f_service_inst", insertable = false, updatable = false)
	private Long serviceInstanceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_server", nullable = false, foreignKey = @ForeignKey(name = "siUser2server"))
	private OServer server;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_service", nullable = false, foreignKey = @ForeignKey(name = "siUser2service"))
	private OService service;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_ars_srvInstUser_user",
		joinColumns = {@JoinColumn(name = "f_srv_inst_user", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_user", nullable = false)},
		foreignKey = @ForeignKey(name = "srvInstUserUser2user"),
		inverseForeignKey = @ForeignKey(name = "srvInstUserUser2siUser")
	)
	private List<User> allowedUsers;

	// ---------------

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_mod", nullable = false))
	private ERowMod rowMod;

	@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "siUser_crtrUsr2user"))
	private User creatorUser;

	@NotAudited
	@Column(name = "f_creator_user")
	private Long creatorUserId;

	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "siUser_mdfrUsr2user"))
	private User modifierUser;

	@Column(name = "f_modifier_user")
	private Long modifierUserId;

	@Version
	@Column(name = "n_version", nullable = false)
	private Integer version = 0;

	// ------------------------------

	public OSIUser() {
	}

	public OSIUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	// ------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getExecutor() {
		return executor;
	}

	public void setExecutor(Boolean executor) {
		this.executor = executor;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public ERemoteMode getRemoteMode() {
		return remoteMode;
	}

	public void setRemoteMode(ERemoteMode remoteMode) {
		this.remoteMode = remoteMode;
	}

	public OServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(OServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

	public Long getServiceInstanceId() {
		return serviceInstanceId;
	}

	public OServer getServer() {
		return server;
	}

	public void setServer(OServer server) {
		this.server = server;
	}

	public OService getService() {
		return service;
	}

	public void setService(OService service) {
		this.service = service;
	}

	public List<User> getAllowedUsers() {
		return allowedUsers;
	}

	public void setAllowedUsers(List<User> allowedUsers) {
		this.allowedUsers = allowedUsers;
	}

	// ---------------

	@Override
	public ERowMod getRowMod() {
		return rowMod;
	}

	@Override
	public void setRowMod(ERowMod rowMod) {
		this.rowMod = rowMod;
	}

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

	public void setCreatorUser(User creatorUser) {
		this.creatorUser = creatorUser;
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

	public void setModifierUser(User modifierUser) {
		this.modifierUser = modifierUser;
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

	// ---------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OSIUser)) return false;

		OSIUser that = (OSIUser) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return String.format("[%s]%s", getUsername(), getServiceInstance());
	}
}
