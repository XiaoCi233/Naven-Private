package tech.blinkfix.modules.impl.render;

public final class WaterMark2Spring {
    private WaterMark2Spring() {
    }

    public static Result step(float current, float target, float velocity, float tension, float friction) {
        float displacement = target - current;
        float force = displacement * tension;
        float drag = velocity * friction;
        float acceleration = force - drag;
        float newVelocity = velocity + acceleration;
        float newPosition = current + newVelocity;
        return new Result(newPosition, newVelocity);
    }

    public static final class Result {
        public final float position;
        public final float velocity;

        public Result(float position, float velocity) {
            this.position = position;
            this.velocity = velocity;
        }
    }
}
