package store.mealforyou.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import store.mealforyou.entity.Member;

import java.util.Collection;
import java.util.List;

public class MemberDetails implements UserDetails {
    private final Member member;
    public MemberDetails(Member member) {
        this.member = member;
    }

    public Long id() {
        return member.getId();
    }
    public String email() {
        return member.getEmail();
    }

    // 권한목록
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override public String getPassword() { return member.getPassword(); } // 암호화된 비밀번호
    @Override public String getUsername() { return member.getEmail(); }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
