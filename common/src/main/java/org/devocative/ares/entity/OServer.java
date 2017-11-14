package org.devocative.ares.entity;

import org.devocative.ares.entity.oservice.OService;
import org.devocative.demeter.entity.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Audited
@Entity
@Table(name = "t_ars_server", uniqueConstraints = {
	@UniqueConstraint(name = "uk_ars_serverName", columnNames = {"c_name"})
})
public class OServer implements ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = -6588853204422299159L;

	@Id
	@GeneratedValue(generator = "ars_server")
	@org.hibernate.annotations.GenericGenerator(name = "ars_server", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ars_server")
		})
	private Long id;

	@Column(name = "c_name", nullable = false)
	private String name;

	@Column(name = "c_address")
	private String address;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_func",
		foreignKey = @ForeignKey(name = "server_func2basic"))
	private OBasicData function;

	@Column(name = "n_counter")
	private Integer counter;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_env",
		foreignKey = @ForeignKey(name = "server_env2basic"))
	private OBasicData environment;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_loc",
		foreignKey = @ForeignKey(name = "server_loc2basic"))
	private OBasicData location;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_comp",
		foreignKey = @ForeignKey(name = "server_comp2basic"))
	private OBasicData company;

	@Column(name = "c_vm_id")
	private String vmId;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_os"))
	private EServerOS serverOS;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_hypervisor", foreignKey = @ForeignKey(name = "server_hyper2server"))
	private OServer hypervisor;

	@Column(name = "f_hypervisor", insertable = false, updatable = false)
	private Long hypervisorId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_owner", foreignKey = @ForeignKey(name = "server_owner2user"))
	private User owner;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "t_ars_service_inst",
		joinColumns = {@JoinColumn(name = "f_service", insertable = false, updatable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_server", insertable = false, updatable = false)}
	)
	private List<OService> services;

	// --------------- CREATE / MODIFY

	@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", nullable = false, insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "server_crtrusr2user"))
	private User creatorUser;

	@NotAudited
	@Column(name = "f_creator_user", nullable = false)
	private Long creatorUserId;

	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "server_mdfrusr2user"))
	private User modifierUser;

	@Column(name = "f_modifier_user")
	private Long modifierUserId;

	@Version
	@Column(name = "n_version", nullable = false)
	private Integer version = 0;

	// ------------------------------

	public OServer() {
	}

	public OServer(Long id) {
		this.id = id;
	}

	public OServer(String name, String address) {
		this.name = name;
		this.address = address;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public OBasicData getFunction() {
		return function;
	}

	public void setFunction(OBasicData function) {
		this.function = function;
	}

	public Integer getCounter() {
		return counter;
	}

	public void setCounter(Integer counter) {
		this.counter = counter;
	}

	public OBasicData getEnvironment() {
		return environment;
	}

	public void setEnvironment(OBasicData environment) {
		this.environment = environment;
	}

	public OBasicData getLocation() {
		return location;
	}

	public void setLocation(OBasicData location) {
		this.location = location;
	}

	public OBasicData getCompany() {
		return company;
	}

	public void setCompany(OBasicData company) {
		this.company = company;
	}

	public String getVmId() {
		return vmId;
	}

	public void setVmId(String vmId) {
		this.vmId = vmId;
	}

	public EServerOS getServerOS() {
		return serverOS;
	}

	public void setServerOS(EServerOS serverOS) {
		this.serverOS = serverOS;
	}

	public OServer getHypervisor() {
		return hypervisor;
	}

	public void setHypervisor(OServer hypervisor) {
		this.hypervisor = hypervisor;
	}

	public Long getHypervisorId() {
		return hypervisorId;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<OService> getServices() {
		return services;
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
		if (!(o instanceof OServer)) return false;

		OServer that = (OServer) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return String.format("%s", getName());
	}
}
