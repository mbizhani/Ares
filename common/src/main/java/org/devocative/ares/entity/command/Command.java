package org.devocative.ares.entity.command;

import org.devocative.ares.entity.oservice.OService;
import org.devocative.ares.vo.xml.XCommand;
import org.devocative.demeter.entity.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_ars_command", uniqueConstraints = {
	@UniqueConstraint(name = "uk_ars_command", columnNames = {"c_name", "f_service"})
})
public class Command implements ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = -63582547132213642L;

	@Id
	@GeneratedValue(generator = "ars_command")
	@org.hibernate.annotations.GenericGenerator(name = "ars_command", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ars_command")
		})
	private Long id;

	@Column(name = "c_name", nullable = false)
	private String name;

	@Column(name = "b_enabled", nullable = false)
	private Boolean enabled = true;

	@Column(name = "b_list_view", nullable = false)
	private Boolean listView;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_service", nullable = false, foreignKey = @ForeignKey(name = "command2service"))
	private OService service;

	@Column(name = "f_service", insertable = false, updatable = false)
	private Long serviceId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_config", nullable = false, foreignKey = @ForeignKey(name = "command2configLob"))
	private CommandCfgLob config;

	@Column(name = "f_config", insertable = false, updatable = false)
	private Long configId;

	// --------------- CREATE / MODIFY

	//@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	//@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", nullable = false, insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "command_crtrUsr2user"))
	private User creatorUser;

	//@NotAudited
	@Column(name = "f_creator_user", nullable = false)
	private Long creatorUserId;

	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "command_mdfrUsr2user"))
	private User modifierUser;

	@Column(name = "f_modifier_user")
	private Long modifierUserId;

	@Version
	@Column(name = "n_version", nullable = false)
	private Integer version = 0;

	// ---------------

	@Transient
	private XCommand xCommand;

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

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getListView() {
		return listView;
	}

	public void setListView(Boolean listView) {
		this.listView = listView;
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

	public CommandCfgLob getConfig() {
		return config;
	}

	public void setConfig(CommandCfgLob config) {
		this.config = config;
	}

	public Long getConfigId() {
		return configId;
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

	// ---------------

	public XCommand getXCommand() {
		return xCommand;
	}

	public void setXCommand(XCommand xCommand) {
		this.xCommand = xCommand;
	}


	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Command)) return false;

		Command that = (Command) o;

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
