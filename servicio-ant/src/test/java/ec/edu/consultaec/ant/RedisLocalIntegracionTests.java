package ec.edu.consultaec.ant;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.consultaec.ant.dominio.UltimoDatoAnt;
import ec.edu.consultaec.ant.infraestructura.RedisUltimoDato;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

/** Solo se ejecuta al solicitar explícitamente la prueba con Redis local. */
@EnabledIfEnvironmentVariable(named = "CONSULTAEC_REDIS_INTEGRATION", matches = "true")
class RedisLocalIntegracionTests {
    @Test
    void escribeLeeYExpiraUnDatoSintetico() throws Exception {
        var conexion = new LettuceConnectionFactory("127.0.0.1", 6379);
        conexion.afterPropertiesSet();
        conexion.start();
        try {
            var redis = new StringRedisTemplate(conexion);
            redis.afterPropertiesSet();
            Set<String> previas = new HashSet<>(redis.keys("consultaec:ant:sim:*"));
            var json = new ObjectMapper().findAndRegisterModules();
            var cache = new RedisUltimoDato(redis, json,
                    "secreto-sintetico-" + UUID.randomUUID(), 2, "SIMULADO");
            var original = new UltimoDatoAnt(json.createObjectNode().put("puntos", 20),
                    Instant.parse("2026-01-01T10:00:00Z"), "SIMULACION_ANT");

            cache.guardar("9999999999", original);
            var recuperado = cache.recuperar("9999999999").orElseThrow();
            assertEquals(original.consultadoEn(), recuperado.consultadoEn());
            assertEquals(20, recuperado.datos().path("puntos").asInt());
            Set<String> nuevas = new HashSet<>(redis.keys("consultaec:ant:sim:*"));
            nuevas.removeAll(previas);
            assertEquals(1, nuevas.size());
            assertTrue(redis.getExpire(nuevas.iterator().next()) > 0);

            Thread.sleep(2400);
            assertTrue(cache.recuperar("9999999999").isEmpty());
        } finally {
            conexion.destroy();
        }
    }
}
