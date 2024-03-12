public class Main {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Command will take only one argument for the path to the assembly file.");
			return;
		}
		Assembler assembler = new Assembler(args[0]);
		if (assembler.assemble()) {
			System.out.println("Compiled to the following path: " + assembler.getOutfile());
		}
	}
}
