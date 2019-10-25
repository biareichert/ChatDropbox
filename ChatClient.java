//dropbox
package com.wsudesc.app;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
//listas
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
//scan
import java.util.Scanner;
//arquivos
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
//sleep
import java.lang.InterruptedException;


public class ChatClient{

private static final String ACCESS_TOKEN = "jCTvGLwEWrkAAAAAAAABAyZMA7k-2nfI8a8VbflYe3I7BI3iyCHqmcKzYCxGtM5M";

private static final String dropboxDir = "dropbox_chat", clienteEntrada="f_entrada", clienteSaida = "f_saida", server_dir="server_dir", extensaoClientEntrada=".client", extensaoClientSaida=".chat", extensaoServer=".serv";
private static String client_dir;

// Create Dropbox client
public static DbxRequestConfig config;
public static DbxClientV2 client;
// Get current account info
public static FullAccount account;

  public List<String> listarDiretoriosDropboxChat_clientes() throws DbxException, IOException{
    ArrayList<String> all = new ArrayList<String>();

    // Get files and folder metadata from Dropbox root directory
    ListFolderResult result = client.files().listFolder("/"+dropboxDir);
    while (true) {
        for (Metadata metadata : result.getEntries()) {
            //System.out.println(metadata.getPathLower());
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
    //receber o diretorio no padrao : /diretorio

    ArrayList<String> all = new ArrayList<String>();

    // Get files and folder metadata from Dropbox root directory
    ListFolderResult result = client.files().listFolder("/"+diretorio);
    while (true) {
        for (Metadata metadata : result.getEntries()) {
            //System.out.println(metadata.getPathLower());
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
         String [] div=arquivo.split("/");//arquivo = /dropbox../f_saida_../nomeArquivo.extensao::: extrair o arquivo

         String nomeArquivo = div[div.length-1];//nome arquivo-numero.extensao
         String [] div2 =nomeArquivo.split("-");

         String nomeDiretorio = div2[0];

        File diretorioClient = new File(destino+"/"+nomeDiretorio);
        if(!diretorioClient.exists()){
           diretorioClient.mkdir();
        }

        System.out.println("nomeArquivo: "+nomeArquivo+"/tnomeDiretorio: "+nomeDiretorio+"/tdestino: "+destino);

        System.out.println("baixando: "+arquivo+" em: "+destino+nomeDiretorio+"/"+nomeArquivo);

          FileOutputStream out = new FileOutputStream(destino+nomeDiretorio+"/"+nomeArquivo);///////////////aqui alterar a extensao .serv para download
          //no server
          downloader.download(out);
          out.close();
          deletarArquivoDropbox(arquivo);
      } catch (DbxException ex) {
          System.out.println(ex.getMessage());
      }
  }

  public void uploadArquivoDropbox(String arquivo, String nomeCliente, String destino, String extensao) throws DbxException, IOException{
     try {

         String nomeArquivo = arquivo;
         if(nomeArquivo.contains("/")){
           String [] div=arquivo.split("/");//arquivo = /dropbox../f_saida_../nomeArquivo.extensao::: extrair o arquivo
          nomeArquivo=div[div.length-1];
        }

         System.out.println("upload: "+nomeCliente+"/"+nomeArquivo+" :: destino: "+destino+"/"+nomeArquivo);

         // if(extensao.compareTo(extensaoClientEntrada)!=0){
         InputStream in = new FileInputStream(client_dir+"/"+nomeCliente+"/"+nomeArquivo);
         FileMetadata metadata = client.files().uploadBuilder(destino+"/"+nomeArquivo).uploadAndFinish(in);

     }catch (DbxException ex) {
          System.out.println(ex.getMessage());
      }

  }

  public static void main(String[] args) throws DbxException, IOException{

    // Create Dropbox client
    config = new DbxRequestConfig(dropboxDir, "en_US");
    client = new DbxClientV2(config, ACCESS_TOKEN);

    // Get current account info
    account = client.users().getCurrentAccount();
    //System.out.println(account.getName().getDisplayName());

    ChatClient cc = new ChatClient();

    List<String> clientes = new ArrayList<String>();
    List<String> diretoriosDropbox = cc.listarArquivosDiretorioDropbox(dropboxDir);

    System.out.println("Bem vindo ao chat!");

    for(String dir : diretoriosDropbox){
// System.out.println(dir);
      if(dir.contains(clienteEntrada)){
        clientes.add(dir.split(clienteEntrada+"_")[1]);// /f_entrada_nomeCliente
        // System.out.println("add cliente: "+dir.split(clienteEntrada+"_")[1]);
      }
    }

    boolean pronto = false; int controle =0;

    while(pronto==false){
      controle=0;
      System.out.println("Informe o seu nome: ");
      client_dir = new Scanner(System.in).next();

      if(clientes.size()==0)
        pronto = true;

      for(String cn : clientes){

        if(cn.compareTo(client_dir)==0){
          controle=1;
          System.out.println("Esse nome ja esta em uso, voce eh: "+ cn +"? (s/n): ");
          String r = new Scanner(System.in).next();
          r= r.toLowerCase();

          if(r.compareTo("s")==0)
            pronto=true;
            break;
        }
      }
      if(controle==0)//n existe nome igual
        pronto=true;

    }

    //criar client dir
    File diretorioClient = new File(client_dir);
    if(!diretorioClient.exists()){
        diretorioClient.mkdir();
    }

    File subDiretorioClient = new File(client_dir+"/"+client_dir);//onde estao as mensagens do cliente
    if(!subDiretorioClient.exists()){
        subDiretorioClient.mkdir();
    }

    //criando os arquivos de entrada e saida para o cliente, caso nao exista
    //String arquivo, String nomeCliente, String destino, String extensao
    // cc.uploadArquivoDropbox("pom.xml",client_dir,"/"+dropboxDir+"/"+clienteSaida+"_"+lient_dir,"");
    // cc.uploadArquivoDropbox("pom.xml",client_dir,"/"+dropboxDir+"/"+clienteEntrada+"_"+client_dir,"");

try{
    InputStream in = new FileInputStream("pom.xml");
    System.out.println("Upload pom.xml em: "+"/"+dropboxDir+"/"+clienteSaida+"_"+client_dir);
    FileMetadata metadata = client.files().uploadBuilder("/"+dropboxDir+"/"+clienteSaida+"_"+client_dir+"/pom.xml").uploadAndFinish(in);
    FileMetadata metadata1 = client.files().uploadBuilder("/"+dropboxDir+"/"+clienteEntrada+"_"+client_dir+"/pom.xml").uploadAndFinish(in);
    Metadata metadata2 = client.files().delete("/"+dropboxDir+"/"+clienteEntrada+"_"+client_dir+"/"+"pom.xml");
    Metadata metadata3 = client.files().delete("/"+dropboxDir+"/"+clienteSaida+"_"+client_dir+"/"+"pom.xml");
}catch (DbxException ex) {
     System.out.println(ex.getMessage());
 }

    List<String> arquivosPUpload = new ArrayList<String>(Arrays.asList(subDiretorioClient.list()));

    String resposta="";
    for(String arq: arquivosPUpload){

       System.out.println("Fazendo Upload do arquivo "+arq+"\n");

       //resposta = new Scanner(System.in).next().toLowerCase();

      //String arquivo, String nomeCliente, String destino, String extensao
       //if(resposta.compareTo("s")==0)
      cc.uploadArquivoDropbox(arq,client_dir,"/"+dropboxDir+"/"+clienteSaida+"_"+client_dir,extensaoClientSaida);

    }
int resposta = -1;
while(resposta != -1){
    System.out.println("Verificar atualizacoes no servidor?\n");
    System.out.println("0 - Sim\n");
    System.out.println("1 - Não\n");
    resposta = new Scanner(System.in).next().toLowerCase();

    if(resposta == 0){
      List<String> arquivosPDownload = cc.listarArquivosDiretorioDropbox("/"+dropboxDir+"/"+clienteEntrada+"_"+client_dir);

      for(String arq : arquivosPDownload){
        //String arquivo, String destino, String extensao
        cc.baixarArquivoDropbox(arq, client_dir+"/",extensaoClientEntrada);//baixar em clientDir/nomeArquivo
        //ideia, no clientDir vai existir o dir de outros clientes
      }
      //System.out.println("Tudo pronto!");
      try{
        Thread.sleep(10000);
      }catch (InterruptedException ex) {
        System.out.println(ex);
      }
    }

}

  }

}

/**
Chat client:

- baixa o que existe em f_entrada do cliente
- faz upload em f_saida do que existe em nomecliente/


*/
