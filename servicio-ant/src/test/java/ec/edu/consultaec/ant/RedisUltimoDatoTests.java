package ec.edu.consultaec.ant;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.consultaec.ant.dominio.UltimoDatoAnt;
import ec.edu.consultaec.ant.infraestructura.RedisUltimoDato;
import java.time.Instant;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class RedisUltimoDatoTests {
    @Test void claveNoRevelaCedulaYSetIncluyeTtl() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> valores = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(valores);
        RedisUltimoDato cache = new RedisUltimoDato(template, new ObjectMapper().findAndRegisterModules(),
                "clave-super-secreta-de-32-caracteres", 86400, "SIMULADO");
        cache.guardar("9999999999", new UltimoDatoAnt(new ObjectMapper().createObjectNode().put("puntos", 20),
                Instant.parse("2026-01-01T10:00:00Z"), "SIMULACION_ANT"));
        ArgumentCaptor<String> clave = ArgumentCaptor.forClass(String.class);
        verify(valores).set(clave.capture(), anyString(), eq(Duration.ofSeconds(86400)));
        assertTrue(clave.getValue().startsWith("consultaec:ant:sim:"));
        assertFalse(clave.getValue().contains("9999999999"));
    }
    @Test void noSeHabilitaRedisSinSecreto() {
        assertThrows(IllegalArgumentException.class, () -> new RedisUltimoDato(
                mock(StringRedisTemplate.class), new ObjectMapper(), "", 86400, "REAL"));
    }
}
