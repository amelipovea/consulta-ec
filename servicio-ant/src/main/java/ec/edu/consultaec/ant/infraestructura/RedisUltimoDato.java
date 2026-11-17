package ec.edu.consultaec.ant.infraestructura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.consultaec.ant.aplicacion.AlmacenUltimoDato;
import ec.edu.consultaec.ant.dominio.UltimoDatoAnt;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "consultaec.redis.habilitado", havingValue = "true")
public class RedisUltimoDato implements AlmacenUltimoDato {
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final Duration ttl;
    private final byte[] secreto;
    private final String espacio;

    public RedisUltimoDato(StringRedisTemplate redis, ObjectMapper json,
            @Value("${consultaec.redis.hmac-secret:}") String secreto,
            @Value("${consultaec.redis.ttl-segundos:86400}") long ttlSegundos,
            @Value("${consultaec.ant.modo:REAL}") String modo) {
        if (secreto == null || secreto.length() < 24) {
            throw new IllegalArgumentException("REDIS_CACHE_HMAC_SECRET debe tener al menos 24 caracteres si Redis está habilitado");
        }
        if (ttlSegundos < 1 || ttlSegundos > 604800) {
            throw new IllegalArgumentException("El TTL debe estar entre 1 y 604800 segundos");
        }
        this.redis = redis;
        this.json = json;
        this.ttl = Duration.ofSeconds(ttlSegundos);
        this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
        this.espacio = "SIMULADO".equalsIgnoreCase(modo) ? "sim" : "real";
    }

    @Override
    public void guardar(String cedula, UltimoDatoAnt dato) {
        try {
            redis.opsForValue().set(clave(cedula), json.writeValueAsString(dato), ttl);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudo serializar la respuesta para Redis", error);
        }
    }

    @Override
    public Optional<UltimoDatoAnt> recuperar(String cedula) {
        String contenido = redis.opsForValue().get(clave(cedula));
        if (contenido == null) return Optional.empty();
        try {
            UltimoDatoAnt dato = json.readValue(contenido, UltimoDatoAnt.class);
            return Optional.of(dato);
        } catch (JsonProcessingException error) {
            return Optional.empty();
        }
    }

    private String clave(String cedula) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto, "HmacSHA256"));
            String hash = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(cedula.getBytes(StandardCharsets.UTF_8)));
            return "consultaec:ant:" + espacio + ":" + hash;
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo preparar la clave de Redis", error);
        }
    }
}
