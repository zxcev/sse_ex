package sse.sse.repo;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmittersRepository {

    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> countMap = new ConcurrentHashMap<>();

    public SseEmitter registerEmitter(Integer userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
        return emitter;
    }

    public void cleanUpEmitter(Integer userId) {
        emitters.remove(userId);
        countMap.remove(userId);
    }

    public int getAndAdd(int userId) {
        Integer value = countMap.getOrDefault(userId, 0);
        countMap.put(userId, value + 1);
        return value;
    }

    public SseEmitter getEmitter(int userId) {
        return emitters.get(userId);
    }
}
