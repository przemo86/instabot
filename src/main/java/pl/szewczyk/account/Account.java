package pl.szewczyk.account;

import pl.szewczyk.instagram.InstaUser;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
@Entity
@Table(name = "account", schema = "instabot")
public class Account implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="user_seq")
	@SequenceGenerator(name="user_seq", sequenceName="user_seq", allocationSize=1, schema = "instabot")
	private Long id;

	@Column(unique = true)
	private String email;

	private String name;
	
	private String password;

	@Enumerated(EnumType.STRING)
	private Role role = Role.ROLE_USER;

	private Date created;

	private Date expires;

	private boolean locked = false;

	@ManyToMany(cascade = CascadeType.ALL)
	private Set<InstaUser> instaUsers = new HashSet<>();

    protected Account() {

	}
	
	public Account(String email, String password, Role role) {
		this.email = email;
		this.password = password;
		this.role = role;
		this.created = new Date();
	}

	public Long getId() {
		return id;
	}

    public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<InstaUser> getInstaUsers() {
		return instaUsers;
	}

	public void setInstaUsers(Set<InstaUser> instaUsers) {
		this.instaUsers = instaUsers;
	}
}
