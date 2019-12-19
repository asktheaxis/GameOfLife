
public class Movie {
	public static void main(String args[]) {
		String[] movies = new String [] {"Tunnel", "Demonic", "HellHouse"};
		int index;
		do {
			index = (int)Math.random();
		} while (index < 2);
		System.out.println(movies[index]);
		System.out.println(movies[0]);
		
	}
}
