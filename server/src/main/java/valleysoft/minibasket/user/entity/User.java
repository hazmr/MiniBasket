package valleysoft.minibasket.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean active;

    private boolean emailVerified;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public User(String email, String passwordHash, Role role) {
        this.email = email;
        this.password = passwordHash;
        this.role = role;
        this.active = true;
        this.emailVerified = false;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void changePassword(String newPasswordHash) {
        this.password = newPasswordHash;
    }
}
