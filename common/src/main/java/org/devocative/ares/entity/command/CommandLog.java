package org.devocative.ares.entity.command;

import org.devocative.ares.entity.oservice.OServiceInstance;
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

	@Column(name = "c_params", length = 1000)
	private String params;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_result", nullable = false))
	private ECommandResult result;

	@Column(name = "n_duration")
	private Long duration;

	@Column(name = "c_error", length = 2000)
	private String error;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_command", nullable = false, insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "commandLog2command"))
	private Command command;

	@Column(name = "f_command")
	private Long commandId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_service_instance", nullable = false, insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "commandLog2serviceInstance"))
	private OServiceInstance serviceInstance;

	@Column(name = "f_service_instance")
	private Long serviceInstanceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_prep_command", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "commandLog2prepCommand"))
	private PrepCommand prepCommand;

	@Column(name = "f_prep_command")
	private Long prepCommandId;

	// --------------- CREATE / MODIFY

	//@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	//@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", nullable = false, insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "commandLog_crtrUsr2user"))
	private User creatorUser;

	//@NotAudited
	@Column(name = "f_creator_user", nullable = false)
	private Long creatorUserId;

	// ------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public ECommandResult getResult() {
		return result;
	}

	public void setResult(ECommandResult result) {
		this.result = result;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
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

	public Long getCommandId() {
		return commandId;
	}

	public void setCommandId(Long commandId) {
		this.commandId = commandId;
	}

	public OServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public Long getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(Long serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public PrepCommand getPrepCommand() {
		return prepCommand;
	}

	public Long getPrepCommandId() {
		return prepCommandId;
	}

	public CommandLog setPrepCommandId(Long prepCommandId) {
		this.prepCommandId = prepCommandId;
		return this;
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
