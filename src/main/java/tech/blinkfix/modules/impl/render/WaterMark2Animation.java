package tech.blinkfix.modules.impl.render;

public final class WaterMark2Animation {
    private WaterMark2Animation() {
    }

    public enum Direction {
        FORWARDS,
        BACKWARDS
    }

    public static final class EaseOutExpo {
        private final long duration;
        private final double end;
        private double start = 0.0;
        private long startTime = System.currentTimeMillis();
        private Direction direction = Direction.FORWARDS;

        public EaseOutExpo(long duration, double end) {
            this.duration = duration;
            this.end = end;
        }

        public void setDirection(Direction dir) {
            if (this.direction != dir) {
                this.direction = dir;
                startTime = System.currentTimeMillis();
                start = getOutput();
            }
        }

        public double getOutput() {
            double progress = (System.currentTimeMillis() - startTime) / (double) duration;
            double result;
            if (direction == Direction.FORWARDS) {
                result = progress >= 1.0 ? end : (-Math.pow(2.0, -10.0 * progress) + 1.0) * end;
            } else {
                result = progress >= 1.0 ? 0.0 : (Math.pow(2.0, -10.0 * progress) * end);
            }
            return Math.max(0.0, Math.min(end, result));
        }
    }

    public static final class SwitchAnimationState {
        private final EaseOutExpo animation = new EaseOutExpo(300, 1.0);

        public void updateState(boolean state) {
            animation.setDirection(state ? Direction.FORWARDS : Direction.BACKWARDS);
        }

        public double getOutput() {
            return animation.getOutput();
        }
    }
}
