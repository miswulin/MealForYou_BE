package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.mealforyou.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {}