package com.wsudesc.app;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.InterruptedException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.nio.file.*;

public class ChatClient{
  private static final String ACCESS_TOKEN = "bV9CF_mwE-AAAAAAAAABTyllMAsCYrSID0reQ-u5pI2U";
  private static final String dropboxDir = "dropbox_chat_bia", clienteEntrada="cliente", clienteSaida = "cliente", server_dir="arquivos_server", extensaoClientEntrada=".client", extensaoClientSaida=".chat", extensaoServer=".serv";
  private static String client_dir;
  public static DbxRequestConfig config;
  public static DbxClientV2 client;
  public static FullAccount account;

  public List<String> listarDiretoriosDropboxChat_clientes() throws DbxException, IOException{
    ArrayList<String> all = new ArrayList<String>();
    ListFolderResult result = client.files().listFolder("/"+dropboxDir);
    while (true) {
        for (Metadata metadata : result.getEntries()) {
            all.add(metadata.getPathLower());
        }
        if (!result.getHasMore()) {
            break;
        }
        result = client.files().listFolderContinue(result.getCursor());
    }
    return all;
  }

  public List<String> listarArquivosDiretorioDropbox(String diretorio) throws DbxException, IOException{
    ArrayList<String> all = new ArrayList<String>();
    ListFolderResult result = client.files().listFolder("/"+diretorio);
    while (true) {
        for (Metadata metadata : result.getEntries()) {
            all.add(metadata.getPathLower());
        }
        if (!result.getHasMore()) {
            break;
        }
        result = client.files().listFolderContinue(result.getCursor());
    }
      return all;
  }

  public void deletarArquivoDropbox(String arquivo) throws DbxException, IOException{
    Metadata metadata = client.files().delete(arquivo);
  }

  public void baixarArquivoDropbox(String arquivo, String destino, String extensao) throws DbxException, IOException{
      DbxDownloader<FileMetadata> downloader = client.files().download(arquivo);
      try {
         String [] div=arquivo.split("/");
         String nomeArquivo = div[div.length-1];
         String [] div2 =nomeArquivo.split("-");
         String nomeDiretorio = div2[0];
         File diretorioClient = new File(destino+"/"+nomeDiretorio);
         if(!diretorioClient.exists()){
            diretorioClient.mkdir();
         }

         //System.out.println("nomeArquivo: "+nomeArquivo+"/tnomeDiretorio: "+nomeDiretorio+"/tdestino: "+destino);
         //System.out.println("baixando: "+arquivo+" em: "+destino+nomeDiretorio+"/"+nomeArquivo);

         FileOutputStream out = new FileOutputStream(destino+nomeDiretorio+"/"+nomeArquivo);
         downloader.download(out);
         out.close();
      } catch (DbxException ex) {
          System.out.println(ex.getMessage());
      }
  }

  public void uploadArquivoDropbox(String arquivo, String nomeCliente, String destino, String extensao) throws DbxException, IOException{
     try {
         String nomeArquivo = arquivo;
         if(nomeArquivo.contains("/")){
           String [] div=arquivo.split("/");
           nomeArquivo=div[div.length-1];
         }

         System.out.println("Fazendo Upload...");
         InputStream in = new FileInputStream(client_dir+"/"+nomeCliente+"/"+nomeArquivo);
         FileMetadata metadata = client.files().uploadBuilder(destino+"/"+nomeArquivo).uploadAndFinish(in);

     }catch (DbxException ex) {
          System.out.println(ex.getMessage());
     }
  }

  public static void main(String[] args) throws DbxException, IOException{
    config = new DbxRequestConfig(dropboxDir, "en_US");
    client = new DbxClientV2(config, ACCESS_TOKEN);
    account = client.users().getCurrentAccount();
    ChatClient cc = new ChatClient();
    List<String> clientes = new ArrayList<String>();
    List<String> diretoriosDropbox = cc.listarArquivosDiretorioDropbox(dropboxDir);

    System.out.println("Bem vindo ao chat!");
    System.out.println("");

    for(String dir : diretoriosDropbox){
      if(dir.contains(clienteEntrada)){
        clientes.add(dir.split(clienteEntrada+"_")[1]);
      }
    }

    boolean pronto = false; int controle =0;

    while(pronto==false){
      controle=0;
      System.out.println("Informe o nome do cliente: ");
      client_dir = new Scanner(System.in).next();

      if(clientes.size()==0)
        pronto = true;

      for(String cn : clientes){

        if(cn.compareTo(client_dir)==0){
          controle=1;
          Scanner ler = new Scanner(System.in);
          System.out.println("Esse nome já está em uso, você é "+ cn+"e está tentando logar novamente? ");
          System.out.println("0 - Sim");
          System.out.println("1 - Não");
          int r = ler.nextInt();

          if(r == 0)
            pronto=true;
            break;
        }
      }
      if(controle==0)
        pronto=true;
    }

    File diretorioClient = new File(client_dir);
    if(!diretorioClient.exists()){
        diretorioClient.mkdir();
    }

    File subDiretorioClient = new File(client_dir+"/"+client_dir);

    if(!subDiretorioClient.exists()){
        subDiretorioClient.mkdir();
    }

    Scanner ler = new Scanner(System.in);
    while(true){

       System.out.println("Informe o nome do arquivo");
       String a = ler.next();
       String conteudoArq = new String(Files.readAllBytes(Paths.get(a)));

       try	{
			       FileWriter arq = new FileWriter(client_dir+"/"+client_dir+"/"+a);
			       PrintWriter gravarArq = new PrintWriter(arq);
			       gravarArq.printf(conteudoArq);
			       arq.close();
		   }catch(Exception ex){
			       ex.printStackTrace();
		   }
       List<String> arquivosPUpload = new ArrayList<String>(Arrays.asList(subDiretorioClient.list()));

       String resposta="";
       for(String arq: arquivosPUpload){
        cc.uploadArquivoDropbox(arq,client_dir,"/"+dropboxDir+"/"+clienteSaida+"_"+client_dir,extensaoClientSaida);
       }

       System.out.println("Verificando Arquivos...");

       List<String> arquivosPDownload = cc.listarArquivosDiretorioDropbox("/"+dropboxDir+"/"+clienteEntrada+"_"+client_dir);

       for(String arq : arquivosPDownload){
          cc.baixarArquivoDropbox(arq, client_dir+"/",extensaoClientEntrada);
       }

       System.out.println("Okay");

       try{
          Thread.sleep(10000);
       }catch (InterruptedException ex) {
          System.out.println(ex);
       }
    }
  }
}
