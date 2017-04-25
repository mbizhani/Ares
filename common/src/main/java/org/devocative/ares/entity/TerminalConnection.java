package org.devocative.ares.entity;

import org.devocative.ares.entity.oservice.OSIUser;
import org.devocative.demeter.entity.ICreationDate;
import org.devocative.demeter.entity.ICreatorUser;
import org.devocative.demeter.entity.User;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_ars_terminal")
public class TerminalConnection implements ICreationDate, ICreatorUser {
	private static final long serialVersionUID = 2130018465964377576L;

	@Id
	@GeneratedValue(generator = "ars_terminal")
	@org.hibernate.annotations.GenericGenerator(name = "ars_terminal", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ars_terminal")
		})
	private Long id;

	@Column(name = "b_active", nullable = false)
	private Boolean active = true;

	@Column(name = "d_disconnection", columnDefinition = "date")
	private Date disconnection;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_target", nullable = false, foreignKey = @ForeignKey(name = "trmConn2osiUser"))
	private OSIUser target;

	// ---------------

	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "trmConn2user"))
	private User creatorUser;

	@Column(name = "f_creator_user")
	private Long creatorUserId;

	// ------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Date getDisconnection() {
		return disconnection;
	}

	public void setDisconnection(Date disconnection) {
		this.disconnection = disconnection;
	}

	public OSIUser getTarget() {
		return target;
	}

	public void setTarget(OSIUser target) {
		this.target = target;
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
}
