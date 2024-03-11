public class Main {
	public static void main(String[] args) {
		Assembler assembler = new Assembler("yasTest.txt");
		assembler.assemble();
		System.out.println(assembler.getOutfile());
	}
}
