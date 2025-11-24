package store.mealforyou.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.mealforyou.dto.PrivatePasswordChangeRequest;
import store.mealforyou.dto.PublicPasswordResetRequest;
import store.mealforyou.entity.Member;
import store.mealforyou.repository.EmailAuthRepository;
import store.mealforyou.repository.MemberRepository;
import store.mealforyou.security.MemberDetails;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordService {

    private final MemberRepository memberRepository; // 회원 조회/저장
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화
    private final EmailAuthRepository emailAuthRepository; // 이메일 인증 여부 확인

    // 비로그인 상태에서의 비밀번호 재설정 메서드
    public void resetPassword(PublicPasswordResetRequest request) {
        // 이메일 인증 여부 확인
        if (!emailAuthRepository.isVerified(request.email())) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        // 새 비밀번호 일치 확인
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }

        // 회원 조회 (여기서 가져오는 Member는 JPA가 관리하는 엔티티)
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 새 비밀번호 암호화 후 저장
        // @Transactional + 영속 엔티티이므로 save() 없이도 flush 시점에 UPDATE 쿼리가 나감
        member.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    // 로그인 상태에서의 비밀번호 변경 메서드
    public void changePassword(MemberDetails memberDetails, PrivatePasswordChangeRequest request) {

        // 로그인 된 사용자의 이메일(or id)을 꺼냄
        String email = memberDetails.email();

        // 이 이메일로 "현재 트랜잭션의 영속 컨텍스트"에서 Member를 다시 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 새 비밀번호 일치 확인
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 저장
        // 여기서도 save() 호출 없이 @Transactional + 영속 엔티티이므로 자동 업데이트
        member.updatePassword(passwordEncoder.encode(request.newPassword()));
    }
}
