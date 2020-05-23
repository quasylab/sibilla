package quasylab.sibilla.core.markov;

import java.util.Arrays;

public class State {
		
		private int[] state;
		
		public State(int ... state) {
			this.state = state;
		}
		
		public int[] getState() {
			return state;
		}
		
		public int retrieve(int idx) {
			return state[idx];
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(state);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			State other = (State) obj;
			if (!Arrays.equals(state, other.state))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return Arrays.toString(state);
		}
}
