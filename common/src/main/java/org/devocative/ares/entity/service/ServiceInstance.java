package org.devocative.ares.entity.service;

import org.devocative.ares.entity.Server;
import org.devocative.demeter.entity.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "t_ars_service_inst")
public class ServiceInstance implements ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
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

	@Column(name = "c_name", nullable = false)
	private String name;

	@Column(name = "n_name")
	private Integer port;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_server", nullable = false, foreignKey = @ForeignKey(name = "srvcinst2server"))
	private Server server;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_service", nullable = false, foreignKey = @ForeignKey(name = "srvcinst2service"))
	private Service service;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_service_inst_related",
		joinColumns = @JoinColumn(name = "f_related"),
		inverseJoinColumns = @JoinColumn(name = "f_source")
	)
	private List<ServiceInstance> related;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "serviceInstance")
	private List<SIPropertyValue> properties;

	// --------------- CREATE / MODIFY

	//@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	//@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "srvcinst_crtrusr2user"))
	private User creatorUser;

	//@NotAudited
	@Column(name = "f_creator_user")
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

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public List<ServiceInstance> getRelated() {
		return related;
	}

	public void setRelated(List<ServiceInstance> related) {
		this.related = related;
	}

	public List<SIPropertyValue> getProperties() {
		return properties;
	}

	public void setProperties(List<SIPropertyValue> properties) {
		this.properties = properties;
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

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ServiceInstance)) return false;

		ServiceInstance that = (ServiceInstance) o;

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
