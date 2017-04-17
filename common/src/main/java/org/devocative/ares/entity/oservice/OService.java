package org.devocative.ares.entity.oservice;

import org.devocative.demeter.entity.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "t_ars_service", uniqueConstraints = {
	@UniqueConstraint(name = "uk_ars_service", columnNames = {"c_name"})
})
public class OService implements ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = -110639388094098969L;

	@Id
	@GeneratedValue(generator = "ars_service")
	@org.hibernate.annotations.GenericGenerator(name = "ars_service", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ars_service")
		})
	private Long id;

	@Column(name = "c_name", nullable = false)
	private String name;

	@Column(name = "c_conn_pattern", length = 1000)
	private String connectionPattern;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "service")
	private List<OServiceProperty> properties;

	// --------------- CREATE / MODIFY

	//@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	//@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "service_crtrusr2user"))
	private User creatorUser;

	//@NotAudited
	@Column(name = "f_creator_user")
	private Long creatorUserId;

	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "service_mdfrusr2user"))
	private User modifierUser;

	@Column(name = "f_modifier_user")
	private Long modifierUserId;

	@Version
	@Column(name = "n_version", nullable = false)
	private Integer version = 0;

	// ------------------------------

	public OService() {
	}

	public OService(String name) {
		this.name = name;
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

	public String getConnectionPattern() {
		return connectionPattern;
	}

	public void setConnectionPattern(String connectionPattern) {
		this.connectionPattern = connectionPattern;
	}

	public List<OServiceProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<OServiceProperty> properties) {
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
		if (!(o instanceof OService)) return false;

		OService that = (OService) o;

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
