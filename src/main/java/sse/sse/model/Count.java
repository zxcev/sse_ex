package sse.sse.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Count {

    private int count;

    public void add() {
        this.count++;
    }
}
