package huffman;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/** 
 * Huffman Compression
 * Department of Computer Science, University of Helsinki
 * Course: Tietorakenteiden harjoitustyö (Data Structures Project)
 * Spring 2011.
 * 
 * @author Juuso Hyvönen
 * 
 */
public class Huffman {

        /**
         * Number of bytes in the decompressed file
         */
        int count;
        
        /**
         * i = byte and freq[i] = frequency of that byte
         */
        int[] freq = new int[256];
        
        /**
         * After generating Huffman code, heap.getMin() is the root of Huffman tree
         */
        Heap heap;
        
        /**
         * Number of different symbols in file
         */
        int numberOfSymbols;
        
        /**
         * i = byte and symbolTable[i] = String representation of 
         * optimal prefix code for the byte.
         */
        String[] symbolTable = new String[256];;        

        /**
         * Choose between code and decode.
         * @param args first parameter chooses between coding and decoding.
         * second parameter is the path to file.
         */
        public Huffman(String[] args) {
                if(args.length < 2) {
                        System.out.println("Give proper parameters\n" +
                                        "for compressing 'java -jar Huffman.jar code example.txt'\n" +
                                        "for decompressing 'java -jar Huffman.jar decode example.txt.huff'");
                        System.exit(0);
                }
                String codeOrDecode = args[0];
                String PATH = args[1];
                if(codeOrDecode.equals("code")) {
                        System.out.println("Huffman coding " + PATH);
                        code(PATH);
                }
                if(codeOrDecode.equals("decode")) {
                        System.out.println("Decoding " + PATH);
                        decode(PATH);           
                }
        }

        /**
         * Compresses a file using Huffman coding.
         * @param PATH path to the file to be Huffman coded.
         */
        private void code(String PATH) {
                getFrequencies(PATH);
                generateTree(); 
                generateCode(heap.getMin(), "");        
                System.out.println("Writing file");
                writeFile(PATH, symbolTable);
                System.out.println("done");
        }

        /**
         * Calculates frequencies of different characters in the file.
         * @param PATH path to file
         */
        private void getFrequencies(String PATH) {
                System.out.println("Calculating frequencies");
                try {
                        readFreqsFromFile(PATH);                        
                }
                catch (FileNotFoundException ex) {
                        System.out.println("File not found");
                        System.exit(0);
                }
                catch (IOException ex) {
                        ex.printStackTrace();
                }
        }

        /**
         * This is where the actual reading of frequencies happens.
         * Simultaneously calculates size of the file.
         * @param PATH path to file
         * @throws FileNotFoundException
         * @throws IOException
         */
        private void readFreqsFromFile(String PATH)
        throws FileNotFoundException, IOException {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(PATH));
                int byt = 0;
                count = 0;
                while ((byt = bis.read()) != -1) {
                        freq[byt] = freq[byt] + 1;
                        count ++;
                }
                bis.close();
        }

        /**
         * Creates optimal prefix tree.
         */
        private void generateTree() {
                System.out.println("Creating heap");
                createHeap(freq);
                numberOfSymbols = heap.getSize();
                System.out.println("Number of different characters: " + numberOfSymbols);
                System.out.println("Creating prefix tree");
                heap = createTree(heap);        
        }

        /**
         * Creates a minimum heap using given frequency table.
         * @param freq frequency table of characters
         */
        private void createHeap(int[] freq) {
                heap = new Heap(257);
                for(int i = 0; i < freq.length; ++i) {
                        if(freq[i] != 0) {
                                Node node = new Node(freq[i], i);
                                node.setToLeaf();
                                heap.insert(node);
                        }
                }
        }

        /**
         * Huffman Algorithm
         * @param heap minimum heap
         * @return huffman tree
         */
        private Heap createTree(Heap heap) {
                int n = heap.getSize();
                for(int i = 0; i < (n-1); ++i) {
                        Node z = new Node();
                        z.setLeft(heap.delMin());
                        z.setRight(heap.delMin());
                        z.setFreq(z.getLeft().getFreq() + z.getRight().getFreq());
                        heap.insert(z);
                }
                return heap;
        }

        /**
         * Generates prefix codes for all characters in the file.
         * @param node root of Huffman tree
         * @param code give empty string to calculate proper code
         */
        private void generateCode(Node node, String code) {             
                if(node != null) {                      
                        if(node.isLeaf())
                                symbolTable[node.getKey()] = code;
                        else {
                                generateCode(node.getLeft(), code + "0");
                                generateCode(node.getRight(), code + "1");
                        }
                }
        }

        /**
         * Writes file using optimal prefix code.
         * @param PATH path to write file
         * @param symbolTable symbols to use
         */
        private void writeFile(String PATH, String[] symbolTable) {
                try {
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(PATH));
                        PATH = PATH + ".huff";
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(PATH));
                        writeHeader(bos);
                        writeBody(bos, bis);
                        bos.close();
                }
                catch(Exception e) {
                        System.out.println(e);
                }
        }

        /**
         * Writes header for the file.
         * First 8 bits = number of different characters.
         * Next 32 bits = length of the file (count)
         * Finally the prefix code. Null acts as a mark between codes.
         * @param bos BufferedOutputStream initialized with the correct path to write file.
         * @throws IOException
         */
        private void writeHeader(BufferedOutputStream bos)
        throws IOException {
                bos.write(numberOfSymbols);
                Tools.writeFullInt(bos, count);
                writeFreqs(bos);
        }

        /**
         * Writes frequencies of characters.
         * @param bos where to write
         * @throws IOException
         */
        private void writeFreqs(BufferedOutputStream bos) throws IOException {
                for (int i = 0; i < symbolTable.length; ++i) {
                        if(symbolTable[i] != null) {
                                bos.write(i);
                                Tools.writeFullInt(bos, freq[i]);
                        }
                }
        }

        /**
         * Writes the actual file using Huffman code.
         * @param bos where to write
         * @param bis the file to compress
         * @throws IOException
         */
        private void writeBody(BufferedOutputStream bos, BufferedInputStream bis) 
        throws IOException {
                String buffer = writeBodyText(bos, bis);
                writeRemainingBuffer(bos, buffer);
        }

        /**
         * Does the actual writing.
         * @param bos where to write
         * @param bis the file to compress
         * @return buffer that was not dividible by 8
         * @throws IOException
         */
        private String writeBodyText(BufferedOutputStream bos,
                        BufferedInputStream bis)
        throws IOException {
                String buffer = "";
                boolean[] bits = new boolean[8];
                int byteRead;
                int byteToWrite = 0;

                while ((byteRead = bis.read()) != -1) {
                        String charToWrite = symbolTable[byteRead];             
                        buffer = buffer + charToWrite;
                        while(buffer.length() >= 8) {   
                                for(int i = 0; i < 8; ++i) {
                                        if(buffer.charAt(i) == '1') 
                                                bits[i] = true;
                                        else
                                                bits[i] = false;
                                }
                                buffer = buffer.substring(8);
                                byteToWrite = Tools.bitsToByte(bits);
                                bos.write(byteToWrite);
                        }                       
                }
                return buffer;
        }

        /**
         * Writes the last byte of file if number of bits was not dividible by 8.
         * @param bos where to write
         * @throws IOException
         */
        private void writeRemainingBuffer(BufferedOutputStream bos, String buffer) throws IOException {
                boolean[] bits = new boolean[8];
                int byteToWrite;
                if(buffer.length() > 0) {
                        int difference = 8 - buffer.length();
                        System.out.println("Number of thrash bits in last byte: " + difference);
                        for(int i = 0; i < buffer.length(); ++i) {
                                if(i > difference) {
                                        bits[i] = false;
                                }
                                else {
                                        if(buffer.charAt(i) == '1')
                                                bits[i] = true;
                                        else
                                                bits[i] = false;
                                }
                        }
                }
                byteToWrite = Tools.bitsToByte(bits);
                bos.write(byteToWrite);
        }

        /**
         * Decodes file compressed in Huffman presuming the header file is identical to the one 
         * defined in writeheader.
         * @param PATH path to file.
         */
        private void decode(String PATH) {
                try {
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(PATH));           
                        String outPutPath = PATH.substring(0, PATH.length()-6); // remove .huff from the name
                        BufferedOutputStream bos = 
                                new BufferedOutputStream(new FileOutputStream(outPutPath + ".decoded"));
                        numberOfSymbols = bis.read();
                        count = Tools.readFullLengthInt(bis);
                        readSymbols(bis);
                        generateTree();
                        writeDecodedFile(bis, bos);
                        System.out.println("done");
                }
                catch(Exception e){
                        System.out.println("File not found");
                        System.exit(0);
                }
        }

        /**
         * Read symbolTable from header
         * @param bis where to read from
         * @throws IOException
         */
        private void readSymbols(BufferedInputStream bis)
        throws IOException {
                symbolTable = new String[256];
                for(int i = 0; i < numberOfSymbols; ++i) {                              
                        int character = bis.read();
                        freq[character] = Tools.readFullLengthInt(bis);
                }
        }

        /**
         * Writes the file using Huffman tree generated just before.
         * @param bis where to read from
         * @param bos where to write
         * @throws IOException
         */
        private void writeDecodedFile(BufferedInputStream bis,
                        BufferedOutputStream bos) throws IOException {
                Node node = heap.getMin();
                int key;
                int character;
                System.out.println("Decompressing file");
                while(true) {
                        character = bis.read();
                        for(int i = 0; i < 8; ++i) {
                                if(node.isLeaf()) {
                                        key = node.getKey();
                                        bos.write(key);
                                        node = heap.getMin();
                                        count--;
                                        if(count == 0) {
                                                break;
                                        }
                                }
                                int bit = (character & 0x80);                   
                                if(bit == 0x80) {
                                        node = node.getRight();
                                }
                                else
                                        node = node.getLeft();
                                character <<= 1;
                        }
                        if(count == 0) {
                                break;
                        }
                }
                bos.close();
        }

        /**
         * It all starts from here!
         * @param args first argument = "code" or "decode"
         * second argument = path to file
         */
        public static void main(String[] args) {
                new Huffman(args);
        }
}