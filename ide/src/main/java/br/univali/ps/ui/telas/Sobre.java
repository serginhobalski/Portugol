package br.univali.ps.ui.telas;

import br.univali.ps.nucleo.PortugolStudio;
import br.univali.ps.ui.swing.ColorController;
import br.univali.ps.ui.swing.Themeable;
import br.univali.ps.ui.utils.FabricaDicasInterface;
import br.univali.ps.ui.utils.IconFactory;
import br.univali.ps.ui.utils.WebConnectionUtils;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.json.JSONObject;

/**
 *
 * @author lite
 */
public class Sobre extends javax.swing.JPanel implements Themeable
{
    
    /**
     * Creates new form Sobre
     */
    public Sobre()
    {
        initComponents();
        
        setSize(750, 550);
//        rotuloDescricao.setText(String.format(rotuloDescricao.getText(), PortugolStudio.getInstancia().getVersao()));

        configurarLinks();
        FabricaDicasInterface.criarTooltip(labellicensa, "Veja as Licensas do Software");
        labellicensa.setIcon(IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "license.png"));
        configurarCores();
        versaoLabel.setText("v"+PortugolStudio.getInstancia().getVersao());
    }
    
    @Override
    public void configurarCores(){
        painelConteudo.setBackground(ColorController.FUNDO_CLARO);
        paineInferior.setBackground(ColorController.FUNDO_ESCURO);
        versaoLabel.setForeground(ColorController.COR_LETRA);
        jPanel1.setBackground(ColorController.FUNDO_ESCURO);
        jLabel1.setBackground(ColorController.COR_PRINCIPAL);
        jLabel1.setText(colocarCSS(carregarHTML("/br/univali/ps/ui/telas/membros.html")));
    }
    
    private void configurarLinks()
    {
        configurarLink(jLabel2);
        configurarLink(rotuloBitRock);
        configurarLink(rotuloGithub);
        configurarLink(rotuloOsi);
        configurarLink(rotuloUnivali);
    }

    private void configurarLink(final JLabel rotulo)
    {
        rotulo.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                WebConnectionUtils.abrirSite(rotulo.getName());
            }
        });
    }
    
    private String carregarHTML(String caminho)
    {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(caminho), Charset.forName("UTF-8")));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        String base = contentBuilder.toString();
        
        return base;
    }
    
    private String colocarCSS(String HTML)
    {
        JSONObject tema = ColorController.TEMA_SELECIONADO;
        
        HTML = HTML.replace("<!--", "").replace("-->", "");
        
        HTML = HTML.replace("${fundo_claro}", tema.getString("fundo_claro"))
                 .replace("${cor_letra_titulo}", tema.getString("cor_letra_titulo"))
                 .replace("${progress_bar}", tema.getString("progress_bar"))
                 .replace("${cor_letra}", tema.getString("cor_letra"))
                 .replace("${fundo_medio}", tema.getString("fundo_medio"))
                 .replace("${icones}", tema.getString("icones"))
                 .replace("${fundo_escuro}", tema.getString("fundo_escuro"))
                 .replace("${cor_destaque}", tema.getString("cor_destaque"))
                 .replace("${cor_console}", tema.getString("cor_console"))
                 .replace("${fundo_botoes_expansiveis}", tema.getString("fundo_botoes_expansiveis"))
                 .replace("${cor_principal}", tema.getString("cor_principal"))
                 .replace("${cor_4}", tema.getString("cor_4"))
                 .replace("${cor_3}", tema.getString("cor_3"))
                 .replace("${cor_2}", tema.getString("cor_2"))
                 .replace("${cor_1}", tema.getString("cor_1"));
        
        JSONObject editor = tema.getJSONObject("Editor");
        
        HTML = HTML.replace("${palavra_reservada}", editor.getString("palavras_reservadas"))
                 .replace("${cursor}", editor.getString("cursor"))
                 .replace("${tipo_reservado}", editor.getString("tipos"))
                 .replace("${selecao_chave_correspondente_fg}", editor.getString("selecao_chave_correspondente_fg"))
                 .replace("${selecao_chave_correspondente_bg}", editor.getString("selecao_chave_correspondente_bg"))
                 .replace("${valor_hexa}", editor.getString("valor_hexa"))
                 .replace("${valor_cadeia}", editor.getString("valor_cadeia"))
                 .replace("${valor_logico}", editor.getString("valor_logico"))
                 .replace("${valor_inteiro}", editor.getString("valor_inteiro"))
                 .replace("${separador}", editor.getString("separador"))
                 .replace("${background_editor}", editor.getString("background_editor"))
                 .replace("${erro_bg}", editor.getString("erro_bg"))
                 .replace("${numeros_das_linhas}", editor.getString("numeros_das_linhas"))
                 .replace("${valor_real}", editor.getString("valor_real"))
                 .replace("${tipos}", editor.getString("tipos"))
                 .replace("${erro_fg}", editor.getString("erro_fg"))
                 .replace("${selecao_linha_atual}", editor.getString("selecao_linha_atual"))
                 .replace("${comentario_linha}", editor.getString("comentario_linha"))
                 .replace("${valor_caracter}", editor.getString("valor_caracter"))
                 .replace("${palavras_reservadas}", editor.getString("palavras_reservadas"))
                 .replace("${comentario_multilinha}", editor.getString("comentario_multilinha"))
                 .replace("${chamada_funcao}", editor.getString("chamada_funcao"))
                 .replace("${borda_barra_lateral}", editor.getString("borda_barra_lateral"))
                 .replace("${dobrador_de_codigo}", editor.getString("dobrador_de_codigo"))
                 .replace("${operador}", editor.getString("operador"))
                 .replace("${selection_bg}", editor.getString("selection_bg"))
                 .replace("${identificador}", editor.getString("identificador"))
                 .replace("${tipo_declaracao}", editor.getString("valor_inteiro"));
        
       return HTML;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainpanel = new javax.swing.JPanel();
        painelConteudo = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        logo1 = new br.univali.ps.ui.imagens.Logo();
        jLabel2 = new javax.swing.JLabel();
        versaoLabel = new javax.swing.JLabel();
        paineInferior = new javax.swing.JPanel();
        rotuloGithub = new javax.swing.JLabel();
        rotuloOsi = new javax.swing.JLabel();
        rotuloUnivali = new javax.swing.JLabel();
        rotuloBitRock = new javax.swing.JLabel();
        labellicensa = new javax.swing.JLabel();

        setRequestFocusEnabled(false);
        setLayout(new java.awt.BorderLayout());

        mainpanel.setLayout(new java.awt.BorderLayout());

        painelConteudo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelConteudo.setLayout(new java.awt.BorderLayout());

        jLabel1.setBackground(new java.awt.Color(102, 102, 255));
        jLabel1.setText("<html>   <head>     <style>       body{         background-color: rgb(38,50,56);         color: rgb(205,205,205);       }       .ativo b{         color: rgb(0,239,192);       }       .inativo b{         color: rgb(255,194,0);       }       h1{         color: rgb(69,189,255);       }     </style>   </head>   <body>     <h1> Membros Ativos </h1>     <div class=\"ativo\">       <b>Adson Marques Da Silva Esteves :</b> Atua no projeto como programador e designer     </div>     <div class=\"ativo\">       <b>Alisson Steffens Henrique :</b> Atua no projeto como programador e designer     </div>     <div class=\"ativo\">       <b>Andr?? Luis Alice Raabe :</b> Atua no projeto como coordenador     </div>     <div class=\"ativo\">       <b>Andr?? Luiz Maciel Santana :</b> Atua no projeto como orientador     </div>     <div class=\"ativo\">       <b>Elieser A. de Jesus :</b> Atua no projeto como programador e designer     </div>     <div class=\"ativo\">       <b>Luiz Fernando Noschang :</b> Atua no projeto como programador e designer     </div>     <div class=\"ativo\">       <b>Paulo Eduardo Martins :</b> Atua no projeto como programador e editor de conte??do     </div>     <h1> Membros Inativos </h1>     <div class=\"inativo\">       <b>Carlos Alexangre Krueger :</b> Atuou no projeto como editor de conte??do     </div>     <div class=\"inativo\">       <b>Fillipi Pelz :</b> Atuou no projeto como programador     </div>     <div class=\"inativo\">       <b>Giordana M. da C. Valle :</b> Atuou no projeto como editora de conte??do     </div>     <div class=\"inativo\">       <b>Nereu Oliveira :</b> Atuou no projeto como programador     </div>     <div class=\"inativo\">       <b>Paula Mannes :</b> Atuou no projeto como programadora     </div>     <div class=\"inativo\">       <b>Roberto Gon??alves Augusto J??nior :</b> Atuou no projeto como editor de conte??do     </div>     </body> </html> ");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        painelConteudo.add(jLabel1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridLayout(0, 1));

        logo1.setMaximumSize(new java.awt.Dimension(310, 100));
        logo1.setMinimumSize(new java.awt.Dimension(310, 100));
        logo1.setOpaque(false);
        jPanel1.add(logo1);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/imagens/logo_lite.png"))); // NOI18N
        jLabel2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel2.setName("http://lite.acad.univali.br/pt/"); // NOI18N
        jPanel1.add(jLabel2);

        painelConteudo.add(jPanel1, java.awt.BorderLayout.WEST);

        versaoLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        versaoLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        versaoLabel.setText("jLabel3");
        painelConteudo.add(versaoLabel, java.awt.BorderLayout.PAGE_END);

        mainpanel.add(painelConteudo, java.awt.BorderLayout.CENTER);

        add(mainpanel, java.awt.BorderLayout.CENTER);

        paineInferior.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        paineInferior.setLayout(new java.awt.GridLayout(1, 0, 5, 5));

        rotuloGithub.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rotuloGithub.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloGithub.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/imagens/logo_github.png"))); // NOI18N
        rotuloGithub.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rotuloGithub.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotuloGithub.setName("https://github.com/"); // NOI18N
        paineInferior.add(rotuloGithub);

        rotuloOsi.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rotuloOsi.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloOsi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/imagens/logo_osi.png"))); // NOI18N
        rotuloOsi.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rotuloOsi.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotuloOsi.setName("http://opensource.org/"); // NOI18N
        paineInferior.add(rotuloOsi);

        rotuloUnivali.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rotuloUnivali.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloUnivali.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/imagens/logo_univali.png"))); // NOI18N
        rotuloUnivali.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rotuloUnivali.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotuloUnivali.setMaximumSize(new java.awt.Dimension(48, 40));
        rotuloUnivali.setMinimumSize(new java.awt.Dimension(48, 40));
        rotuloUnivali.setName("https://www.univali.br/graduacao/ciencia-da-computacao-itajai/Paginas/default.aspx"); // NOI18N
        paineInferior.add(rotuloUnivali);

        rotuloBitRock.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rotuloBitRock.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloBitRock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/imagens/logo_bitrock.png"))); // NOI18N
        rotuloBitRock.setToolTipText("");
        rotuloBitRock.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rotuloBitRock.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotuloBitRock.setName("http://installbuilder.bitrock.com/"); // NOI18N
        paineInferior.add(rotuloBitRock);

        labellicensa.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labellicensa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/univali/ps/ui/icones/Dark/grande/license.png"))); // NOI18N
        labellicensa.setToolTipText("");
        labellicensa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labellicensa.setMaximumSize(new java.awt.Dimension(48, 40));
        labellicensa.setMinimumSize(new java.awt.Dimension(48, 40));
        labellicensa.setPreferredSize(null);
        labellicensa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labellicensaMouseClicked(evt);
            }
        });
        paineInferior.add(labellicensa);

        add(paineInferior, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void labellicensaMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_labellicensaMouseClicked
    {//GEN-HEADEREND:event_labellicensaMouseClicked
        JDialog telaLicencas = PortugolStudio.getInstancia().getTelaLicencas();
        telaLicencas.setVisible(true);
    }//GEN-LAST:event_labellicensaMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labellicensa;
    private br.univali.ps.ui.imagens.Logo logo1;
    private javax.swing.JPanel mainpanel;
    private javax.swing.JPanel paineInferior;
    private javax.swing.JPanel painelConteudo;
    private javax.swing.JLabel rotuloBitRock;
    private javax.swing.JLabel rotuloGithub;
    private javax.swing.JLabel rotuloOsi;
    private javax.swing.JLabel rotuloUnivali;
    private javax.swing.JLabel versaoLabel;
    // End of variables declaration//GEN-END:variables
}
