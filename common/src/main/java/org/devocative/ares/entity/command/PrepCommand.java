package org.devocative.ares.entity.command;

import org.devocative.ares.entity.oservice.OServiceInstance;
import org.devocative.demeter.entity.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Audited
@Entity
@Table(name = "t_ars_prp_command", uniqueConstraints = {
	@UniqueConstraint(name = "uk_ars_prepCommand_code", columnNames = {"c_code"})
})
public class PrepCommand implements ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = 2051551440648987293L;

	@Id
	@GeneratedValue(generator = "ars_prp_command")
	@org.hibernate.annotations.GenericGenerator(name = "ars_prp_command", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ars_prp_command")
		})
	private Long id;

	@Column(name = "c_name", nullable = false)
	private String name;

	@Column(name = "c_code", nullable = false)
	private String code;

	@Column(name = "c_params", length = 1000)
	private String params;

	@Column(name = "b_enabled", nullable = false)
	private Boolean enabled = true;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_command", nullable = false, foreignKey = @ForeignKey(name = "prpCommand2command"))
	private Command command;

	@NotAudited
	@Column(name = "f_command", nullable = false, insertable = false, updatable = false)
	private Long commandId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_service_instance", foreignKey = @ForeignKey(name = "prpCommand2serviceInstance"))
	private OServiceInstance serviceInstance;

	@Column(name = "f_service_instance", insertable = false, updatable = false)
	private Long serviceInstanceId;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_ars_prpCommand_user",
		joinColumns = {@JoinColumn(name = "f_prp_command", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_user", nullable = false)},
		foreignKey = @ForeignKey(name = "prpCommandUser2prpCommand"),
		inverseForeignKey = @ForeignKey(name = "prpCommandUser2user")
	)
	private List<User> allowedUsers;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_ars_prpCommand_role",
		joinColumns = {@JoinColumn(name = "f_prp_command", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_role", nullable = false)},
		foreignKey = @ForeignKey(name = "prpCommandRole2prpCommand"),
		inverseForeignKey = @ForeignKey(name = "prpCommandRole2role")
	)
	private List<Role> allowedRoles;

	// ---------------

	@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "prpCommand_crtrUsr2user"))
	private User creatorUser;

	@NotAudited
	@Column(name = "f_creator_user")
	private Long creatorUserId;

	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "prpCommand_mdfrUsr2user"))
	private User modifierUser;

	@Column(name = "f_modifier_user")
	private Long modifierUserId;

	@Version
	@Column(name = "n_version", nullable = false)
	private Integer version = 0;

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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public Long getCommandId() {
		return commandId;
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

	// ---------------

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
		if (!(o instanceof PrepCommand)) return false;

		PrepCommand that = (PrepCommand) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return getName();
	}
}
