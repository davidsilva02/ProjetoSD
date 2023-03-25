// package tests_random;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.io.ObjectInputStream;
// import java.io.ObjectOutputStream;
// import java.util.ArrayList;
// import java.io.File;

// class array_list {
//     public static ArrayList <Integer>  nomes;
//     public static void main(String[] args) {    
//         File names = new File("./tests_random/Names.obj");
        
//         if(!names.exists()){
//             nomes=new ArrayList<>();
//             nomes.add(2);
//             nomes.add(3);
//             nomes.add(4);
    
//             for(Integer n: nomes){
//                 System.out.println(n);
//             }


//             try {
//                 // an OutputStream file
//                 // "namesListData" is
//                 // created
//                 FileOutputStream fos= new FileOutputStream(names);
      
//                 // an ObjectOutputStream object is
//                 // created on the FileOutputStream
//                 // object
//                 ObjectOutputStream oos= new ObjectOutputStream(fos);
      
//                 // calling the writeObject()
//                 // method of the
//                 // ObjectOutputStream on the
//                 // OutputStream file "namesList"
//                 oos.writeObject(nomes);
      
//                 // close the ObjectOutputStream
//                 oos.close();
      
//                 // close the OutputStream file
//                 fos.close();
      
//                 System.out.println("namesList serialized");
//             }
//             catch (IOException ioe) {
//                 ioe.printStackTrace();
//             }
//         }

//         else{
//             try (
//                 FileInputStream fis = new FileInputStream(names);
//                 ObjectInputStream ois = new ObjectInputStream(fis);) {
    
//                 nomes = (ArrayList) ois.readObject();
//             } catch (IOException ioe) {
//             ioe.printStackTrace();
//             } catch (ClassNotFoundException c) {
//             System.out.println("Class not found");
//             c.printStackTrace();
//             }
    
//             for(Integer n: nomes){
//                 System.out.println(n);
//             }
//         }
        
        
//     }
// }
