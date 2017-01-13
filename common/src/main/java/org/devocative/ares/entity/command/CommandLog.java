package org.devocative.ares.entity.command;

import org.devocative.demeter.entity.ICreationDate;
import org.devocative.demeter.entity.ICreatorUser;
import org.devocative.demeter.entity.User;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_ars_command_log")
public class CommandLog implements ICreationDate, ICreatorUser {
	private static final long serialVersionUID = -3093416910803442241L;

	@Id
	@GeneratedValue(generator = "ars_command_log")
	@org.hibernate.annotations.GenericGenerator(name = "ars_command_log", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ars_command_log")
		})
	private Long id;

	@Column(name = "c_param", nullable = false, length = 1000)
	private String param;

	@Column(name = "b_successful", nullable = false)
	private Boolean successful;

	@Column(name = "c_error", length = 2000)
	private String error;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_command", nullable = false, foreignKey = @ForeignKey(name = "commandLog2command"))
	private Command command;

	// --------------- CREATE / MODIFY

	//@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	//@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "commandLog_crtrUsr2user"))
	private User creatorUser;

	//@NotAudited
	@Column(name = "f_creator_user")
	private Long creatorUserId;

	// ------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public Boolean getSuccessful() {
		return successful;
	}

	public void setSuccessful(Boolean successful) {
		this.successful = successful;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
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

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CommandLog)) return false;

		CommandLog that = (CommandLog) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		//return  != null ?  : String.format("[%s]", getId());
		return null;
	}
}
