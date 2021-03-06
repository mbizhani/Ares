package org.devocative.ares.entity.oservice;

import org.devocative.demeter.entity.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.Date;

@Audited
@Entity
@Table(name = "t_ars_service_inst_prop_val", uniqueConstraints = {
	@UniqueConstraint(name = "uk_ars_siPropVal", columnNames = {"f_property", "f_service_inst"})
})
public class OSIPropertyValue implements ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = 8592429112321349167L;

	@Id
	@GeneratedValue(generator = "ars_service_inst_prop_val")
	@org.hibernate.annotations.GenericGenerator(name = "ars_service_inst_prop_val", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ars_service_inst_prop_val")
		})
	private Long id;

	@Column(name = "c_value", nullable = false)
	private String value;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_property", nullable = false, foreignKey = @ForeignKey(name = "siPropVal2property"))
	private OServiceProperty property;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_service_inst", nullable = false, foreignKey = @ForeignKey(name = "siPropVal2serviceInstance"))
	private OServiceInstance serviceInstance;

	// --------------- CREATE / MODIFY

	@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", nullable = false, insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "siPropVal_crtrUsr2user"))
	private User creatorUser;

	@NotAudited
	@Column(name = "f_creator_user", nullable = false)
	private Long creatorUserId;

	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "siPropVal_mdfrUsr2user"))
	private User modifierUser;

	@Column(name = "f_modifier_user")
	private Long modifierUserId;

	@Version
	@Column(name = "n_version", nullable = false)
	private Integer version = 0;

	// ------------------------------

	public OSIPropertyValue() {
	}

	public OSIPropertyValue(OServiceProperty property, OServiceInstance serviceInstance) {
		this.property = property;
		this.serviceInstance = serviceInstance;
	}

	public OSIPropertyValue(String value, OServiceProperty property, OServiceInstance serviceInstance) {
		this.value = value;
		this.property = property;
		this.serviceInstance = serviceInstance;
	}

	// ------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public OServiceProperty getProperty() {
		return property;
	}

	public void setProperty(OServiceProperty property) {
		this.property = property;
	}

	public OServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(OServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
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
		if (!(o instanceof OSIPropertyValue)) return false;

		OSIPropertyValue that = (OSIPropertyValue) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return String.format("%s = %s", getProperty(), getValue());
	}
}
