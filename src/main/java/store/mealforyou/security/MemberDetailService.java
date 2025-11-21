package store.mealforyou.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import store.mealforyou.repository.MemberRepository;

// 스프링 시큐리티가 인증을 수행할 때 호출하는 서비스
@Service
@RequiredArgsConstructor
public class MemberDetailService implements UserDetailsService {
    private final MemberRepository memberRepository;

    // 스프링 시큐리티 규약 메서드
    // - username 파라미터: 로그인 시 클라이언트가 보낸 아이디 (== 이메일)
    // - return: MemberDetails 구현체 (MemberDetails)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 존재하지 않으면 UsernameNotFoundException 던져 인증 실패로 연결
        var member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("이메일을 찾을 수 없습니다: " + email));

        // Member 엔티티를 시큐리티 표준 모델(MemberDetails)로 감싸서 반환
        // (내부에서 getUsername()은 이메일, getPassword()는 BCrypt 해시를 제공함)
        return new MemberDetails(member);
    }
}
