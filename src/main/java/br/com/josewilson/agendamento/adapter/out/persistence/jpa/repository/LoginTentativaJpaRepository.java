package br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.LoginTentativaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface LoginTentativaJpaRepository extends JpaRepository<LoginTentativaJpaEntity, String> {

    /** Upsert atomico no banco: duas replicas falhando ao mesmo tempo nao perdem contagem. */
    @Modifying(clearAutomatically = true)
    @Query(value = """
            INSERT INTO login_tentativa (email, falhas, bloqueado_ate)
            VALUES (:email, 1, CASE WHEN 1 >= CAST(:maximo AS integer) THEN CAST(:ate AS timestamptz) END)
            ON CONFLICT (email) DO UPDATE SET
                falhas = login_tentativa.falhas + 1,
                bloqueado_ate = CASE WHEN login_tentativa.falhas + 1 >= CAST(:maximo AS integer)
                                     THEN CAST(:ate AS timestamptz) END
            """, nativeQuery = true)
    void registrarFalha(@Param("email") String email, @Param("maximo") int maximo, @Param("ate") Instant ate);
}
