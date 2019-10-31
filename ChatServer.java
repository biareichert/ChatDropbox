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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.InterruptedException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.nio.file.*;

public class ChatServer{

  private static final String ACCESS_TOKEN = "bV9CF_mwE-AAAAAAAAABTyllMAsCYrSID0reQ-u5pI2U375noLTnfGhTov48M04b";
  private static final String dropboxDir = "dropbox_chat_bia", clienteEntrada="cliente", clienteSaida = "cliente", server_dir="arquivos_server", extensaoClientEntrada=".client", extensaoClientSaida=".chat", extensaoServer=".serv";
  //server_dir é o nome do diretorio do servidor local e dropbox (mesmo nome)

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
          System.out.println("baixando: "+arquivo);

          String [] div=arquivo.split("/");//arquivo = /dropbox../f_saida_../nomeArquivo.extensao::: extrair o arquivo

          String nomeArquivo = div[div.length-1];
          nomeArquivo=nomeArquivo.replace(extensaoClientSaida, extensao);

          FileOutputStream out = new FileOutputStream(destino+"/"+nomeArquivo);///////////////aqui alterar a extensao .serv para download no server
          downloader.download(out);
          out.close();
      } catch (DbxException ex) {
          System.out.println(ex.getMessage());
      }
  }

  public void uploadArquivoDropbox(String arquivo, String nomeCliente, String destino, String extensao, String nome) throws DbxException, IOException{
      try {
          String [] div=arquivo.split("/");//arquivo = /dropbox../f_saida_../nomeArquivo.extensao::: extrair o arquivo
          String nomeArquivo = div[div.length-1];
          String conteudoArq = new String(Files.readAllBytes(Paths.get(nomeArquivo)));
          System.out.println(conteudoArq);
          String novoNome = "";
          if(extensao.equals(".client")){
            novoNome = nome + "-" + nomeCliente + ".client";
          }else{
            if(extensao.equals(".serv")){
              novoNome = nomeCliente+extensaoServer;
            }
          }

          System.out.println("novoNome: "+novoNome);
          System.out.println("extensao: "+extensao);
          System.out.println("destino: "+destino);

          try	{
  			       FileWriter arq = new FileWriter(server_dir+"/"+novoNome);
  			       PrintWriter gravarArq = new PrintWriter(arq);
  			       gravarArq.printf(conteudoArq);
  			       arq.close();
  		   }catch(Exception ex){
  			       ex.printStackTrace();
  		   }

          //nomeArquivo=nomeArquivo.replace(extensaoClientSaida, extensaoServer);
          System.out.println("upload: "+server_dir+"/"+novoNome+" :: destino: "+dropboxDir+"/"+novoNome);
          // if(extensao.compareTo(extensaoClientEntrada)!=0){
          InputStream in = new FileInputStream(server_dir+"/"+novoNome);
          FileMetadata metadata = client.files().uploadBuilder("/"+dropboxDir+"/"+novoNome).uploadAndFinish(in);
      }catch (DbxException ex) {
          System.out.println(ex.getMessage());
      }
  }

  public static void main(String[] args) throws DbxException, IOException{
      config = new DbxRequestConfig(dropboxDir, "en_US");
      client = new DbxClientV2(config, ACCESS_TOKEN);
      account = client.users().getCurrentAccount();
      File diretorioServer = new File(server_dir);

      if(!diretorioServer.exists()){
          diretorioServer.mkdir();
      }
      ChatServer cs = new ChatServer();

      while(true){
      //  System.out.println("Verificando atualizacoes no servidor....Nao fechar!");
        List<String> diretoriosDropbox = cs.listarDiretoriosDropboxChat_clientes();
        List<String> clientes = new ArrayList<String>();

        //encontrar os clientes que ja acessaram o dropbox
        for(String dir : diretoriosDropbox){
          if(dir.contains(clienteEntrada)){
            clientes.add(dir.split(clienteEntrada+"_")[1]);// /f_entrada_nomeCliente
          }
        }
        //criar o diretorio fisico em server_dir
        //String nomeCli;

        for(String cl : clientes){
          String teste = server_dir+"/"+cl;
        //  System.out.println(teste);
          File diretorio = new File(server_dir+"/"+cl);
          if(!diretorio.exists()){
            diretorio.mkdir();
          }

          List<String> clienteArquivos = cs.listarArquivosDiretorioDropbox("/"+dropboxDir+"/"+clienteSaida+"_"+cl);//retorna o caminho completo de cada arquivo

          for(String ca : clienteArquivos){
            //cs.baixarArquivoDropbox(ca,server_dir+"/"+cl,extensaoServer);
            cs.deletarArquivoDropbox(ca);

            for(String cl1 : clientes){

              if(cl1.compareTo(cl)==0){//nao enviar para o mesmo cliente de que se foi baixado
                cs.uploadArquivoDropbox(ca,cl,"/"+dropboxDir+"/"+server_dir+"/"+cl, extensaoServer, cl);
                continue;
              }

              cs.uploadArquivoDropbox(ca,cl,"/"+dropboxDir+"/"+clienteEntrada+"_"+cl1, extensaoClientEntrada, cl1);
            }

          }

        }

        System.out.println("Tudo pronto!");
        try{
          Thread.sleep(20000);
        }catch (InterruptedException ex) { System.out.println(ex); }
      }

    }
}
