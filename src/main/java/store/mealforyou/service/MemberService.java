package store.mealforyou.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.mealforyou.constant.ProductTag;
import store.mealforyou.dto.*;
import store.mealforyou.entity.Member;
import store.mealforyou.repository.MemberRepository;
import store.mealforyou.security.MemberDetails;
import store.mealforyou.util.PhoneNumberNormalizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션
public class MemberService {

    private final MemberRepository memberRepository;
    private final PhoneNumberNormalizer phoneNumberNormalizer;

    // 이메일로 로그인한 사용자의 Member 엔티티를 조회하는 메서드
    // 존재하지 않으면 GlobalExceptionHandler에서 400으로 응답
    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    // 마이페이지 상단 프로필 정보 조회 메서드
    // 이름, 이메일, 전화번호, 주소, 선호 식단 태그를 한 번에 반환
    public MemberProfileResponse getMyProfile(MemberDetails memberDetails) {
        Member member = getMemberByEmail(memberDetails.email());

        // Address -> AddressDTO 반환
        AddressDTO addressDTO = null;
        if (member.getAddress() != null){
            addressDTO = new AddressDTO(
                    member.getAddress().getZipCode(),
                    member.getAddress().getRoadAddress(),
                    member.getAddress().getDetailAddress()
            );
        }

        // healthTags(Set) -> List로 변환 (JSON 배열로 내려주기 위해)
        List<ProductTag> tags = new ArrayList<>(member.getHealthTags());

        return new MemberProfileResponse(
                member.getEmail(),
                member.getName(),
                member.getPhoneE164(),
                addressDTO,
                tags
        );
    }

    // 회원정보 수정 (이름, 전화번호)
    @Transactional
    public void updateMemberInfo(MemberDetails memberDetails, MemberInfoUpdateRequest request) {
        Member member = getMemberByEmail(memberDetails.email());

        // 이름 변경
        member.updateName(request.name());

        // 전화번호는 회원가입과 동일하게 E.164 형식으로 정규화 후 저장
        String phoneE164 = phoneNumberNormalizer.toE164(request.phoneRaw(), "KR");
        member.updatePhone(phoneE164);
    }

    // 기본 배송지 수정
    // null이라면 주소 제거도 허용
    @Transactional
    public void updateAddress(MemberDetails memberDetails, AddressDTO addressDTO) {
        Member member = getMemberByEmail(memberDetails.email());

        if (addressDTO == null) {
            member.updateAddress(null);
            return;
        }

        member.updateAddress(addressDTO.toEmbeddable());
    }

    // 선호 식단 태그 조회
    public HealthTagsResponse getHealthTags(MemberDetails memberDetails) {
        Member member = getMemberByEmail(memberDetails.email());
        List<ProductTag> tags = new ArrayList<>(member.getHealthTags());
        return new HealthTagsResponse(tags);
    }

    // 선호 식단 태그 수정
    @Transactional
    public void updateHealthTags(MemberDetails memberDetails, HealthTagsUpdateRequest request) {
        Member member = getMemberByEmail(memberDetails.email());

        Set<ProductTag> tags = new HashSet<>();
        if (request.healthTags() != null && !request.healthTags().isEmpty()) {
            tags.addAll(request.healthTags());
        }

        member.updateHealthTags(tags);
    }
}
