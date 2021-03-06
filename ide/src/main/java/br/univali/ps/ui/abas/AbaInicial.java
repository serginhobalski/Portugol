package br.univali.ps.ui.abas;

import br.univali.ps.nucleo.PortugolStudio;
import br.univali.ps.ui.paineis.PainelExemplos;
import br.univali.ps.ui.swing.ColorController;
import br.univali.ps.ui.swing.Themeable;
import br.univali.ps.ui.utils.WebConnectionUtils;
import br.univali.ps.ui.swing.weblaf.jOptionPane.QuestionDialog;
import br.univali.ps.ui.telas.TelaPrincipal;
import java.awt.Cursor;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public final class AbaInicial extends Aba implements Themeable
{

    private Action acaoExibirAtalhosTeclado;
    private Action acaoExibirDicasInterface;
    private boolean redimensionouParaBaixaResolucao = false;
    private javax.swing.JLabel rotuloMostrarAlertas;

    public AbaInicial(TelaPrincipal telaPrincipal)
    {
        super();
        this.setName("abainicial");
        setPainelTabulado(telaPrincipal.getPainelTabulado());
        setCabecalho(new BotoesControleAba(this, telaPrincipal));
        initComponents();
        configurarBotaoAlertas();
        configurarCores();
        configurarResolucao();
        configurarCursorLogos();
        criarDicasInterface();
        configurarAcoes();
        configurarLinks();
        instalarObservadorCombinacoesSecretas();
        instalarAcoesSecretas();
    }

    @Override
    public void configurarCores()
    {
        
        painelCentral.setBackground(ColorController.FUNDO_CLARO);
        conteudoColaborar.setBackground(ColorController.FUNDO_CLARO);
        rotuloAjudarDesenvolvimento.setBackground(ColorController.FUNDO_MEDIO);
        rotuloAjudarDesenvolvimento.setForeground(ColorController.COR_LETRA);
        rotuloAtalhosTeclado.setBackground(ColorController.FUNDO_MEDIO);
        rotuloAtalhosTeclado.setForeground(ColorController.COR_LETRA);
        rotuloDicasInterface.setBackground(ColorController.FUNDO_MEDIO);
        rotuloDicasInterface.setForeground(ColorController.COR_LETRA);
        rotuloInformacoesSoftware.setBackground(ColorController.FUNDO_MEDIO);
        rotuloInformacoesSoftware.setForeground(ColorController.COR_LETRA);
        rotuloRelatarBug.setBackground(ColorController.FUNDO_MEDIO);
        rotuloRelatarBug.setForeground(ColorController.COR_LETRA);
        rotuloMostrarAlertas.setBackground(ColorController.FUNDO_MEDIO);
        rotuloMostrarAlertas.setForeground(ColorController.COR_LETRA);
        //        rotuloSlogan.setForeground(new Color(250, 250, 250));
    }
    
    private void configurarBotaoAlertas()
    {
        rotuloMostrarAlertas = new javax.swing.JLabel();
        rotuloMostrarAlertas.setBackground(new java.awt.Color(210, 231, 252));
        rotuloMostrarAlertas.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        rotuloMostrarAlertas.setForeground(new java.awt.Color(51, 51, 51));
        rotuloMostrarAlertas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloMostrarAlertas.setText("<html><body><div>Mostrar Alertas</div></body></html>");
        rotuloMostrarAlertas.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 8, 0));
        rotuloMostrarAlertas.setName("dicasInterface"); // NOI18N
        conteudoColaborar.add(rotuloMostrarAlertas);
    }

    private void instalarObservadorCombinacoesSecretas()
    {
        final StringBuilder sb = new StringBuilder();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher()
        {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e)
            {
                if (AbaInicial.this.getPainelTabulado().getAbaSelecionada() == AbaInicial.this && e.getID() == KeyEvent.KEY_PRESSED)
                {
                    if (Character.isLetterOrDigit(e.getKeyCode()))
                    {
                        sb.append(e.getKeyChar());
                    }
                    else if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        synchronized (AbaInicial.this)
                        {
                            String nome = sb.toString().toUpperCase();
                            Action acao = getActionMap().get(nome);

                            sb.delete(0, sb.length());

                            if (acao != null)
                            {
                                acao.actionPerformed(null);
                            }
                        }
                    }
                }

                return false;
            }
        });
    }

    private void instalarAcoesSecretas()
    {
        instalarAcaoEaster1();
    }

    private void instalarAcaoEaster1()
    {
        getActionMap().put("RICK", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    ImageIcon gif = new ImageIcon(new URL("http://lite.acad.univali.br/~alice/portugol/resources/rick/image.gif"));
                    if (gif.getIconWidth() > 1)
                    {
                        QuestionDialog.getInstance().showMessage("Eita!!!");
                    }
                }
                catch (Exception ex)
                {
                    Logger.getLogger(AbaInicial.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
    }

    private void configurarResolucao()
    {
        addComponentListener(new ComponentAdapter()
        {

            @Override
            public void componentResized(ComponentEvent e)
            {
                SwingUtilities.invokeLater(() -> 
                        {
                            if (getSize().width <= 800 || getSize().height <= 600)
                            {
                                if (!redimensionouParaBaixaResolucao)
                                {
                                    redimensionouParaBaixaResolucao = true;
        
//                                    rotuloSairProgramando.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
//                                    rotuloSairProgramando.setHorizontalTextPosition(javax.swing.SwingConstants.TRAILING);
//                                    rotuloAssistirVideoAulas.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
//                                    rotuloAssistirVideoAulas.setHorizontalTextPosition(javax.swing.SwingConstants.TRAILING);
//                                    rotuloConhecerBibliotecas.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
//                                    rotuloConhecerBibliotecas.setHorizontalTextPosition(javax.swing.SwingConstants.TRAILING);
//                                    rotuloConhecerLinguagem.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
//                                    rotuloConhecerLinguagem.setHorizontalTextPosition(javax.swing.SwingConstants.TRAILING);
                                    //FabricaDicasInterface.mostrarNotificacao("Utilize uma resolu????o maior para melhor uso do Portugol Studio", IconFactory.createIcon(IconFactory.CAMINHO_ICONES_GRANDES, "stop.png"));
                                }
                            }
                            else
                            {
                                redimensionouParaBaixaResolucao = false;
        
//                                rotuloSairProgramando.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
//                                rotuloSairProgramando.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
//                                rotuloAssistirVideoAulas.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
//                                rotuloAssistirVideoAulas.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
//                                rotuloConhecerBibliotecas.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
//                                rotuloConhecerBibliotecas.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
//                                rotuloConhecerLinguagem.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
//                                rotuloConhecerLinguagem.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
                            }
                });
            }

        });
    }

    private void abrirGitHub()
    {
        WebConnectionUtils.abrirSite("https://github.com/UNIVALI-LITE/Portugol-Studio");
    }

    private void configurarCursorLogos()
    {
//        logoUnivali.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        logoLite.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void configurarAcoes()
    {
        configurarAcaoExibirTelaSobre();
        configurarAcaoAjudarDesenvolvimento();
        configurarAcaoRelatarBug();
        configurarAcaoExibirAtalhosTeclado();
        configurarAcaoDicasInterface();
        configurarAcaoMostrarAlertas();
    }

    

    private void configurarAcaoExibirTelaSobre()
    {
        Action acao = new AbstractAction(rotuloInformacoesSoftware.getName())
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PortugolStudio.getInstancia().getTelaSobre().setVisible(true);
            }
        };

        getActionMap().put(rotuloInformacoesSoftware.getName(), acao);
    }

    private void configurarAcaoAjudarDesenvolvimento()
    {
        Action acao = new AbstractAction(rotuloAjudarDesenvolvimento.getName())
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                abrirGitHub();
            }
        };

        getActionMap().put(rotuloAjudarDesenvolvimento.getName(), acao);
    }

    private void configurarAcaoRelatarBug()
    {
        Action acao = new AbstractAction(rotuloRelatarBug.getName())
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PortugolStudio.getInstancia().getTelaRelatarBug().setVisible(true);
            }
        };

        getActionMap().put(rotuloRelatarBug.getName(), acao);
    }

    private void configurarLinks()
    {
        MouseListener listener = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                JLabel rotulo = (JLabel) e.getSource();
                String nomeAcao = rotulo.getName();

                Action acao = getActionMap().get(nomeAcao);

                if (acao != null)
                {
                    acao.actionPerformed(null);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                JLabel rotulo = (JLabel) e.getSource();
                rotulo.setBackground(ColorController.COR_DESTAQUE);
                rotulo.setOpaque(true);
                //rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD));

            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                JLabel rotulo = (JLabel) e.getSource();
                rotulo.setBackground(ColorController.COR_PRINCIPAL);
                rotulo.setOpaque(false);
                //rotulo.setFont(rotulo.getFont().deriveFont(Font.PLAIN));
            }
        };

        rotuloAjudarDesenvolvimento.addMouseListener(listener);
        rotuloAjudarDesenvolvimento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rotuloRelatarBug.addMouseListener(listener);
        rotuloRelatarBug.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rotuloAtalhosTeclado.addMouseListener(listener);
        rotuloAtalhosTeclado.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rotuloDicasInterface.addMouseListener(listener);
        rotuloDicasInterface.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rotuloInformacoesSoftware.addMouseListener(listener);
        rotuloInformacoesSoftware.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        rotuloMostrarAlertas.addMouseListener(listener);
        rotuloMostrarAlertas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    }

    private void configurarAcaoExibirAtalhosTeclado()
    {
        String nome = "atalhosTeclado";
        KeyStroke atalho = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);

        acaoExibirAtalhosTeclado = new AbstractAction(nome)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            PortugolStudio.getInstancia().getTelaAtalhosTeclado().setVisible(true);
            }
        };

        acaoExibirAtalhosTeclado.putValue(Action.ACCELERATOR_KEY, atalho);

        getActionMap().put(nome, acaoExibirAtalhosTeclado);

        getPainelTabulado().getActionMap().put(nome, acaoExibirAtalhosTeclado);
        getPainelTabulado().getInputMap(WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
    }

    private void configurarAcaoDicasInterface()
    {
        String nome = "dicasInterface";
        KeyStroke atalho = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);

        acaoExibirDicasInterface = new AbstractAction(nome)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PortugolStudio.getInstancia().getTelaDicas().setVisible(true);
            }
        };

        acaoExibirDicasInterface.putValue(Action.ACCELERATOR_KEY, atalho);

        getActionMap().put(nome, acaoExibirDicasInterface);

        getPainelTabulado().getActionMap().put(nome, acaoExibirDicasInterface);
        getPainelTabulado().getInputMap(WHEN_IN_FOCUSED_WINDOW).put(atalho, nome);
    }
    
    private void configurarAcaoMostrarAlertas()
    {
        Action acao = new AbstractAction(rotuloMostrarAlertas.getName())
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PortugolStudio.getInstancia().apresentarAlertas(true);
            }
        };

        getActionMap().put(rotuloMostrarAlertas.getName(), acao);
    }

    public PainelExemplos getPainelExemplos() {
        return painelExemplos;
    }
    
    private void criarDicasInterface()
    {
//        FabricaDicasInterface.criarDicaInterface(logoUnivali, "Conhecer o curso de Ci??ncia da Computa????o da UNIVALI");
//        FabricaDicasInterface.criarDicaInterface(logoLite, "Conhecer o Laborat??rio de Inova????o Tecnol??gica na Educa????o");
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        painelCentral = new javax.swing.JPanel();
        painelExemplos = new br.univali.ps.ui.paineis.PainelExemplos();
        conteudoColaborar = new javax.swing.JPanel();
        rotuloDicasInterface = new javax.swing.JLabel();
        rotuloAtalhosTeclado = new javax.swing.JLabel();
        rotuloRelatarBug = new javax.swing.JLabel();
        rotuloAjudarDesenvolvimento = new javax.swing.JLabel();
        rotuloInformacoesSoftware = new javax.swing.JLabel();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        painelCentral.setOpaque(false);
        painelCentral.setLayout(new java.awt.GridBagLayout());

        painelExemplos.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        painelCentral.add(painelExemplos, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(painelCentral, gridBagConstraints);

        conteudoColaborar.setOpaque(false);
        conteudoColaborar.setLayout(new javax.swing.BoxLayout(conteudoColaborar, javax.swing.BoxLayout.X_AXIS));

        rotuloDicasInterface.setBackground(new java.awt.Color(210, 231, 252));
        rotuloDicasInterface.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        rotuloDicasInterface.setForeground(new java.awt.Color(51, 51, 51));
        rotuloDicasInterface.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloDicasInterface.setText("<html><body><div>Dicas Interface (F3)</div></body></html>");
        rotuloDicasInterface.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 8, 0));
        rotuloDicasInterface.setName("dicasInterface"); // NOI18N
        conteudoColaborar.add(rotuloDicasInterface);

        rotuloAtalhosTeclado.setBackground(new java.awt.Color(210, 231, 252));
        rotuloAtalhosTeclado.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        rotuloAtalhosTeclado.setForeground(new java.awt.Color(51, 51, 51));
        rotuloAtalhosTeclado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloAtalhosTeclado.setText("<html><body><div>Atalhos do teclado (F11)</div></body></html>");
        rotuloAtalhosTeclado.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 8, 0));
        rotuloAtalhosTeclado.setName("atalhosTeclado"); // NOI18N
        conteudoColaborar.add(rotuloAtalhosTeclado);

        rotuloRelatarBug.setBackground(new java.awt.Color(210, 231, 252));
        rotuloRelatarBug.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        rotuloRelatarBug.setForeground(new java.awt.Color(51, 51, 51));
        rotuloRelatarBug.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloRelatarBug.setText("<html><body><div>Relatar um Bug</div></body></html>");
        rotuloRelatarBug.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 8, 0));
        rotuloRelatarBug.setName("relatarBug"); // NOI18N
        conteudoColaborar.add(rotuloRelatarBug);

        rotuloAjudarDesenvolvimento.setBackground(new java.awt.Color(210, 231, 252));
        rotuloAjudarDesenvolvimento.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        rotuloAjudarDesenvolvimento.setForeground(new java.awt.Color(51, 51, 51));
        rotuloAjudarDesenvolvimento.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloAjudarDesenvolvimento.setText("<html><body><div>Contribuir</div></body></html>");
        rotuloAjudarDesenvolvimento.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 8, 0));
        rotuloAjudarDesenvolvimento.setName("ajudarDesenvolvimento"); // NOI18N
        conteudoColaborar.add(rotuloAjudarDesenvolvimento);

        rotuloInformacoesSoftware.setBackground(new java.awt.Color(210, 231, 252));
        rotuloInformacoesSoftware.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        rotuloInformacoesSoftware.setForeground(new java.awt.Color(51, 51, 51));
        rotuloInformacoesSoftware.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloInformacoesSoftware.setText("<html><body><div>Sobre (F12)</div></body></html>");
        rotuloInformacoesSoftware.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 8, 0));
        rotuloInformacoesSoftware.setName("informacoesSoftware"); // NOI18N
        conteudoColaborar.add(rotuloInformacoesSoftware);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(conteudoColaborar, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel conteudoColaborar;
    private javax.swing.JPanel painelCentral;
    private br.univali.ps.ui.paineis.PainelExemplos painelExemplos;
    private javax.swing.JLabel rotuloAjudarDesenvolvimento;
    private javax.swing.JLabel rotuloAtalhosTeclado;
    private javax.swing.JLabel rotuloDicasInterface;
    private javax.swing.JLabel rotuloInformacoesSoftware;
    private javax.swing.JLabel rotuloRelatarBug;
    // End of variables declaration//GEN-END:variables
}
