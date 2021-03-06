package br.univali.ps.ui.inspetor;

import br.univali.portugol.nucleo.analise.semantica.AnalisadorDeclaracaoTamanhoVetorMatriz;
import br.univali.portugol.nucleo.analise.semantica.erros.ErroExpressaoTamanhoVetorMatriz;
import br.univali.portugol.nucleo.asa.VisitanteNulo;
import br.univali.portugol.nucleo.programa.Programa;
import br.univali.portugol.nucleo.asa.ExcecaoVisitaASA;
import br.univali.portugol.nucleo.asa.No;
import br.univali.portugol.nucleo.asa.NoDeclaracaoBase;
import br.univali.portugol.nucleo.asa.NoDeclaracaoInicializavel;
import br.univali.portugol.nucleo.asa.NoDeclaracaoMatriz;
import br.univali.portugol.nucleo.asa.NoDeclaracaoParametro;
import br.univali.portugol.nucleo.asa.NoDeclaracaoVariavel;
import br.univali.portugol.nucleo.asa.NoDeclaracaoVetor;
import br.univali.portugol.nucleo.asa.NoExpressao;
import br.univali.portugol.nucleo.asa.NoInteiro;
import br.univali.portugol.nucleo.asa.NoMatriz;
import br.univali.portugol.nucleo.asa.NoOperacao;
import br.univali.portugol.nucleo.asa.NoReferenciaVariavel;
import br.univali.portugol.nucleo.asa.NoVetor;
import br.univali.portugol.nucleo.asa.Quantificador;
import br.univali.portugol.nucleo.asa.TipoDado;
import br.univali.portugol.nucleo.asa.TrechoCodigoFonte;
import br.univali.portugol.nucleo.execucao.ObservadorExecucao;
import br.univali.portugol.nucleo.execucao.ResultadoExecucao;
import br.univali.ps.nucleo.Configuracoes;
import br.univali.ps.nucleo.PortugolStudio;
import br.univali.ps.ui.abas.AbaCodigoFonte;
import br.univali.ps.ui.rstautil.ProcuradorDeDeclaracao;
import br.univali.ps.ui.swing.ColorController;
import com.alee.laf.WebLookAndFeel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.border.Border;

/**
 * @author elieser
 */
public class InspetorDeSimbolos extends JList<ItemDaLista> implements ObservadorExecucao 
{

    private static final Logger LOGGER = Logger.getLogger(InspetorDeSimbolos.class.getName());
    private static final String INSTRUCAO = "Arraste uma vari??vel para este \npain??l se quiser inspecion??-la";
    private final DefaultListModel<ItemDaLista> model = new DefaultListModel<>();

    protected static ItemDaLista ultimoItemModificado = null;

    boolean programaExecutando = false;

    private final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(10, 0, 10, 0);

    private JTextArea textArea; // necess??rio para tratar a importa????o de vari??veis para o inspetor de s??mbolos diretamente do c??digo fonte
    
    private Programa programa; // refer??ncia para o programa compilado, utilizada para procurar vari??veis no programa quando o usu??rio arrasta uma vari??vel do c??digo fonte para o inspetor de s??mbolos

    private final Timer timerAtualizacao;
    private static final int TEMPO_ATUALIZACAO = 250;
    private int indexHovered = -1;
    
    public InspetorDeSimbolos() 
    {
        model.clear();
        setModel(model);
        setDropMode(DropMode.ON);
        setTransferHandler(new TratadorDeArrastamento());
        setCellRenderer(new RenderizadorDaLista());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        instalaObservadores();
        
        timerAtualizacao = new Timer(TEMPO_ATUALIZACAO, (ev) -> { // atualiza inspetor a cada TEMPO_ATUALIZACAO ms quando o programa est?? executando
            atualizaValoresVariaveisInspecionadas();
        });
        
        timerAtualizacao.setRepeats(true);
    }

    public void reseta() 
    {
        model.clear();
        resetaDestaqueDosSimbolos();
    }
    
    public void setPrograma(Programa programa)
    {
        /***
            Sempre que o c??digo fonte ?? alterado a AbaCodigoFonte seta o 'programa' no inspetor de s??mbolos.
            Toda a ??rvore sint??tica ?? recriada e ?? necess??rio verificar se os s??mbolos que 
            est??o no inspetor ainda existem no c??digo. Tamb??m ?? poss??vel que os s??mbolos 
            tenham sido renomeados, ou que o tipo deles tenha mudado.
        */ 
    
        if (this.programa == programa)
        {
            return;
        }
        
        this.programa = programa;
        
        if (!model.isEmpty()) // s?? resconstr??i a lista de s??mbolos se existem s??mbolos sendo inspecionados
        {
            Runnable tarefa = new TarefaReconstrucaoNosInspecionados();
            
            // S?? executa a reconstru????o dos n??s inpecionados com invokeLater se j?? n??o estiver na EDT.
            // Com isso evitamos executar a tarefa na EDT, mesmo j?? estando nela, fazendo com que a tarefa 
            // seja executa s?? depois de algum tempo, o que j?? ?? tarde demais para a inicializa????o dos s??mbolos.
            
            if (SwingUtilities.isEventDispatchThread()) {
                tarefa.run();
            }
            else {
                SwingUtilities.invokeLater(tarefa);
            }
        }
    }
    
    private void instalaObservadores() 
    {
        addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent fe) {
                clearSelection();
            }

        });
        
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent ke) 
            {
                if (ke.getKeyCode() == KeyEvent.VK_DELETE) 
                {
                    int indices[] = getSelectedIndices();
                    int modelSize = model.getSize();
                    for (int i = indices.length - 1; i >= 0; i--) 
                    {
                        int indice = indices[i];
                        if (indice >= 0 && indice < modelSize) 
                        {
                            model.remove(indice);
                        }
                    }
                }
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = new Point(e.getX(),e.getY());
                int index = locationToIndex(p);
                if(indexHovered != index)
                {
                    indexHovered = locationToIndex(p);
                }
            }
        });
    }

    public List<NoDeclaracaoBase> getNosInspecionados() 
    {
        List<NoDeclaracaoBase> nosInpecionados = new ArrayList<>();
        
        for (int i = 0; i < model.getSize(); i++) {
            nosInpecionados.add(model.get(i).getNoDeclaracao());
        }
        
        return nosInpecionados;
    }

    public void setTextArea(JTextArea textArea) 
    {
        this.textArea = textArea;
    }

    private void desenhaInstrucaoParaArrastarSimbolos(Graphics g) 
    {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(getForeground());
        
        FontMetrics metrics = g.getFontMetrics();
        String texto = INSTRUCAO.replace("\n", "");
        int larguraInstrucao = metrics.stringWidth(texto);
        
        if (larguraInstrucao <= getWidth())  // pode desenhar tudo na mesma linha?
        {
            int x = getWidth() / 2 - larguraInstrucao / 2;
            g.drawString(texto, x, getHeight() / 2);
        } 
        else 
        {   // separa o texto em v??ras linhas
            
            String[] linhas = INSTRUCAO.split("\n");
            
            int y = getHeight() / 2 - (linhas.length / 2 * metrics.getHeight());
            
            for (int i = 0; i < linhas.length; i++) {
                String string = linhas[i].trim();
                int x = getWidth() / 2 - metrics.stringWidth(string) / 2;
                g.drawString(string, x, y);
                y += metrics.getHeight();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        if (getModel().getSize() <= 0) {
            desenhaInstrucaoParaArrastarSimbolos(g);
        }
    }

    public void setTamanhoDaFonte(float tamanho) 
    {
        RenderizadorBase.setTamanhoDaFonte(tamanho);
        recriaCacheDaAlturaDosItems();
        repaint();
    }

    void recriaCacheDaAlturaDosItems() 
    {
        // hack para for??ar a JList a refazer a cache. Sem essas linhas o componente n??o reflete a mudan??a no tamanho da fonte adequadamente.
        // Id??ia retirada desse post: http://stackoverflow.com/questions/7306295/swing-jlist-with-multiline-text-and-dynamic-height?lq=1
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setFixedCellHeight(10); // provavelmente poderia ser qualquer outro valor positivo
                setFixedCellHeight(-1);
            }
        });

    }

    private class RenderizadorDaLista implements ListCellRenderer<ItemDaLista> 
    {

        private final JPanel panel = new JPanel(new BorderLayout());

        public RenderizadorDaLista() {
            panel.setBorder(EMPTY_BORDER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ItemDaLista> list, ItemDaLista item, int index, boolean selected, boolean hasFocus) 
        {
            RenderizadorBase renderizador = item.getRendererComponent();

            renderizador.setOpaque(true);

            panel.removeAll();
            panel.add(renderizador, BorderLayout.CENTER); // o componente que renderiza o item da lista foi inserido em um painel e este painel 
            // usa uma EmptyBorder para separar verticalmente os items da lista, assim os items n??o ficam muito "grudados" uns nos outros.

            boolean pintaSelecao = hasFocus || list.getSelectionModel().isSelectedIndex(index);
            if (pintaSelecao) 
            {
                if(Configuracoes.getInstancia().isTemaDark())
                {
                    panel.setBackground(ColorController.FUNDO_ESCURO);
                }
                else{
                    panel.setBackground(ColorController.COR_DESTAQUE);
                }
                
            }
            else if(index == indexHovered) 
            {
                panel.setBackground(ColorController.COR_CONSOLE);
            }
            else
            {
                panel.setBackground(ColorController.TRANSPARENTE);
            }
            return panel;
            //existem 3 tipos de ItemDaLista (para vari??veis, para vetores e para matrizes)
            //cada subclasse de ItemDaLista retorna um renderer component diferente.
        }
    }

    private void limpaValores()
    {
        for (int i = 0; i < model.getSize(); i++) {
            model.getElementAt(i).limpa();
        }        
    }
    
    @Override
    public void execucaoEncerrada(Programa programa, ResultadoExecucao resultadoExecucao) 
    {
        Runnable tarefa = () -> {
            programaExecutando = false;
            timerAtualizacao.stop();
            atualizaValoresVariaveisInspecionadas();
            ultimoItemModificado = null;
            resetaDestaqueDosSimbolos();
            
            atualizaEscopoVariaveisInspecionadas();
        };
        
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(tarefa);
        }
        else {
            tarefa.run();
        }
    }

    private void setStatusDoDestaqueNosSimbolosInspecionados(boolean statusDoDestaque) 
    {
        for (int i = 0; i < model.getSize(); i++) {
            model.get(i).setDesenhaDestaques(statusDoDestaque);
        }
    }

    public void resetaDestaqueDosSimbolos() 
    {
        setStatusDoDestaqueNosSimbolosInspecionados(false);
        repaint();
    }

    /**
     * *
     * desenha apenas as regi??es dos items que podem ser repintados. Os itens
     * s??o repintados apenas umas poucas vezes por segundo para evitar problemas
     * de desempenho quando o usu??rio estiver inspecionados vari??veis que s??o
     * alteradas v??rias vezes por segundo em um jogo, por exemplo.
     */
    private void redesenhaItemsDaLista() 
    {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                int totalDeItems = model.getSize();
                int offset = 0;
                for (int i = 0; i < totalDeItems; i++) {
                    ItemDaLista item = model.getElementAt(i);
                    RenderizadorBase renderizador = item.getRendererComponent();
                    int alturaDoRenderizador = renderizador.getAlturaPreferida();
                    Rectangle bounds = new Rectangle(0, 0, getWidth(), alturaDoRenderizador);

                    Insets insets = EMPTY_BORDER.getBorderInsets(renderizador);
                    offset += insets.top;
                    bounds.translate(0, offset);//desloca o ret??ngulo para a posi????o vertical onde o item est?? na lista
                    repaint(bounds);
                    offset += bounds.height + insets.bottom;
                }
            }
        });

    }

    public boolean contemNo(NoDeclaracaoBase no) 
    {
        if (no == null) {
            return false;
        }
        
        return getItemDoNo(no) != null;
    }

    private ItemDaLista getItemDoNo(NoDeclaracaoBase no) 
    {
        for (int i = 0; i < model.getSize(); i++) {
            ItemDaLista item = model.getElementAt(i);
            if (mesmoNo(item.getNoDeclaracao(), no)) {
                return item;
            }
        }
        
        return null;
    }

    @Override
    public void execucaoIniciada(Programa programa) 
    {
        programaExecutando = true;
        
        limpaValores();
        
        setStatusDoDestaqueNosSimbolosInspecionados(false);
        
        atualizaEscopoVariaveisInspecionadas();
        
        timerAtualizacao.start();
    }

    private void atualizaValoresVariaveisInspecionadas()
    {
        Runnable tarefa = () -> {
            
            if (programa == null)
            {
                return;
            }
            
            for (int i = 0; i < model.getSize(); i++) 
            {
                ItemDaLista item = model.getElementAt(i);
                item.atualiza(programa);
                
                item.setDesenhaDestaques(true);
            }
        
            redesenhaItemsDaLista();
        };
        
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(tarefa);
        }
        else {
            tarefa.run();
        }
    }
    
    @Override
    public void highlightLinha(int linha) 
    {
        atualizaValoresVariaveisInspecionadas();
    }

    @Override
    public void execucaoPausada() 
    {
        atualizaValoresVariaveisInspecionadas();
    }

    @Override
    public void escopoModificado(String escopo)
    {
        atualizaEscopoVariaveisInspecionadas(escopo);
    }
    
    @Override
    public void execucaoResumida() {
        
    }
    
    @Override
    public void highlightDetalhadoAtual(int linha, int coluna, int tamanho) {
        
    }

    private void atualizaEscopoVariaveisInspecionadas()
    {
        atualizaEscopoVariaveisInspecionadas("");
    }
    
    private void atualizaEscopoVariaveisInspecionadas(String escopoAtual)
    {
        //System.out.println("Escopo atual: " + escopoAtual);
        
        for (int i = 0; i < model.getSize(); i++) {
            ItemDaLista item = model.get(i);
            NoDeclaracaoBase no = item.getNoDeclaracao();
            boolean noEstaNoEscopoAtual = true;
            if (programaExecutando) {
                noEstaNoEscopoAtual = noEstaNoEscopoAtual(escopoAtual, no);
            }
            item.setEscopoAtual(noEstaNoEscopoAtual);
           
            //System.out.println(String.format("\t%s => %s => %s", no.getNome(), noEstaNoEscopoAtual, no.getEscopo()));
        }   
    }

    private boolean noEstaNoEscopoAtual(String escopoAtual, No no)
    {
        boolean noEhVariavelGlobal = !no.temPai();
        if (noEhVariavelGlobal)
            return true;
        
        if (escopoAtual.isEmpty())
            return true;

        String escopoNoPai = no.getPai().getEscopo();
        
        return escopoAtual.contains(escopoNoPai);
    }
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private class TratadorDeArrastamento extends TransferHandler 
    {

        private boolean podeImportartNosArrastadosDaTree(TransferHandler.TransferSupport support) 
        {
            List<NoDeclaracaoBase> nosTransferidos = null;
            try 
            {
                nosTransferidos = (List<NoDeclaracaoBase>) support.getTransferable().getTransferData(AbaCodigoFonte.NoTransferable.NO_DATA_FLAVOR);
                for (NoDeclaracaoBase no : nosTransferidos) 
                {
                    if (!contemNo(no)) {
                        support.setShowDropLocation(true);
                        return true;//basta que um dos n??s transferidos ainda n??o esteja no inspetor e deve ser poss??vel adicionar este n?? na lista
                    }
                }
            } catch (IOException | UnsupportedFlavorException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
            
            return false;
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) 
        {
            DataFlavor flavors[] = support.getDataFlavors();
            boolean podeImportar = false;
            for (DataFlavor flavor : flavors) {
                // aceita apenas texto (que ?? arrastado do editor) ou uma lista de NoDeclaracao
                if (flavor.isFlavorTextType() || flavor == AbaCodigoFonte.NoTransferable.NO_DATA_FLAVOR) {
                    podeImportar = true;
                    break;
                }
            }
            if (!support.isDrop() || !podeImportar) {
                return false;
            }

            boolean arrastandoNosDaJTree = support.getTransferable().isDataFlavorSupported(AbaCodigoFonte.NoTransferable.NO_DATA_FLAVOR);
            if (arrastandoNosDaJTree) {
                return podeImportartNosArrastadosDaTree(support);
            }

            return true; // suporta importa????o de string
        }

        private boolean importaNosArrastadosDaJTree(TransferHandler.TransferSupport support) 
        {
            List<NoDeclaracaoBase> nosTransferidos = null;
            try 
            {
                nosTransferidos = (List<NoDeclaracaoBase>) support.getTransferable().getTransferData(AbaCodigoFonte.NoTransferable.NO_DATA_FLAVOR);

                boolean importou = false;
                for (NoDeclaracaoBase noTransferido : nosTransferidos) 
                {
                    if (!contemNo(noTransferido)) {
                        adicionaNo(noTransferido);
                        importou = true;
                    }
                }
                return importou;
            } 
            catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            }
            
            return false;
        }

        private boolean importaStringArrastada(TransferHandler.TransferSupport support) 
        {
            try 
            {
                String stringArrastada = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                if (stringArrastada.equals(textArea.getSelectedText())) {
                    if (stringArrastada.isEmpty() || programa == null) {
                        return false;
                    }
                    int linha = textArea.getLineOfOffset(textArea.getSelectionStart()) + 1;
                    int coluna = textArea.getSelectionStart() - textArea.getLineStartOffset(linha - 1);
                    int tamanhoDoTexto = textArea.getSelectionEnd() - textArea.getSelectionStart();
                    ProcuradorDeDeclaracao procuradorDeDeclaracao = new ProcuradorDeDeclaracao(stringArrastada, linha, coluna, tamanhoDoTexto);
                    programa.getArvoreSintaticaAbstrata().aceitar(procuradorDeDeclaracao);
                    NoDeclaracaoBase no = procuradorDeDeclaracao.getNoDeclaracao();
                    if (procuradorDeDeclaracao.encontrou() && !contemNo(no)) {
                        adicionaNo(no);
                    }
                }
            } catch (Exception e) {
                return false;
            }
            
            return false;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) 
        {
            if (!canImport(support)) 
            {
                return false;
            }

            boolean arrastandoNosDaJTree = support.getTransferable().isDataFlavorSupported(AbaCodigoFonte.NoTransferable.NO_DATA_FLAVOR);
            boolean importou;
            if (arrastandoNosDaJTree) 
            {
                importou = importaNosArrastadosDaJTree(support);
            }
            else 
            {
                importou = importaStringArrastada(support);
            }
            
            return importou;
        }
    }

    private boolean adicionaNoVariavel(NoDeclaracaoVariavel noTransferido) 
    {
        ItemDaListaParaVariavel item = new ItemDaListaParaVariavel((NoDeclaracaoVariavel) noTransferido);
        model.addElement(item);
        if (programa != null)
        {
            programa.inspecionaVariavel(noTransferido.getIdParaInspecao());
        }
        
        return true;
    }

    private boolean adicionaNoVetor(NoDeclaracaoVetor declaracaoVetor) throws ExcecaoVisitaASA 
    {
        int colunas = -1;
        if (declaracaoVetor.getTamanho() != null) {
            colunas = obtemValorDeExpressaoDoTipoInteiro(declaracaoVetor.getTamanho());
        } else if (declaracaoVetor.getInicializacao() != null) {
            colunas = ((NoVetor) declaracaoVetor.getInicializacao()).getValores().size();
        }
        if (colunas > 0) {
            ItemDaListaParaVetor item = new ItemDaListaParaVetor(colunas, declaracaoVetor);
            model.addElement(item);
            if (programa != null)
            {
                programa.inspecionaVetor(declaracaoVetor.getIdParaInspecao(), colunas);
            }
            return true;
        }
        
        return false;
    }

    private Integer obtemValorDeExpressaoDoTipoInteiro(NoExpressao expressao) throws ExcecaoVisitaASA 
    {
        if (expressao == null) {
            return null;
        }

        if (expressao instanceof NoInteiro) {
            return ((NoInteiro) expressao).getValor();
        }

        // se a express??o ?? uma refer??ncia para uma vari??vel ?? necess??rio encontrar a declara????o da vari??vel para obter o seu valor
        if (expressao instanceof NoReferenciaVariavel) 
        {
            if (programa == null) // se n??o existe um programa ent??o n??o ?? poss??vel encontrar um s??mbolo
            {
                return null;
            }

            NoReferenciaVariavel noReferencia = (NoReferenciaVariavel) expressao;
            TrechoCodigoFonte trechoFonte = noReferencia.getTrechoCodigoFonte();
            int linha = trechoFonte.getLinha();
            int coluna = trechoFonte.getColuna();
            int tamanho = trechoFonte.getTamanhoTexto();
            String nomeDoSimbolo = noReferencia.getNome();
            
            ProcuradorDeDeclaracao procuradorDeDeclaracao = new ProcuradorDeDeclaracao(nomeDoSimbolo, linha, coluna, tamanho);
            programa.getArvoreSintaticaAbstrata().aceitar(procuradorDeDeclaracao);
            
            if (procuradorDeDeclaracao.encontrou()) {

                NoDeclaracaoBase noDeclaracao = procuradorDeDeclaracao.getNoDeclaracao();

                if (noDeclaracao instanceof NoDeclaracaoInicializavel) {
                    return obtemValorDeExpressaoDoTipoInteiro(((NoDeclaracaoInicializavel) noDeclaracao).getInicializacao());
                }
            }
        }
        
        if(expressao instanceof NoOperacao)
        {
            AnalisadorDeclaracaoTamanhoVetorMatriz analisadorDeclaracaoTamanhoVetorMatriz = new AnalisadorDeclaracaoTamanhoVetorMatriz();
            try {
                return analisadorDeclaracaoTamanhoVetorMatriz.possuiExpressaoDeTamanhoValida(null, expressao);
            } catch (ErroExpressaoTamanhoVetorMatriz ex) {
                PortugolStudio.getInstancia().getTratadorExcecoes().exibirExcecao(ex);
            }
        }
        return null;
    }

    private boolean adicionaNoMatriz(NoDeclaracaoMatriz noTransferido) throws ExcecaoVisitaASA 
    {
        int colunas = -1, linhas = -1;
        if (noTransferido.getNumeroColunas() != null && noTransferido.getNumeroLinhas() != null) 
        {
            colunas = obtemValorDeExpressaoDoTipoInteiro(noTransferido.getNumeroColunas());
            linhas = obtemValorDeExpressaoDoTipoInteiro(noTransferido.getNumeroLinhas());
        } 
        else if (noTransferido.getInicializacao() != null) 
        {
            List<List<Object>> valores = ((NoMatriz) noTransferido.getInicializacao()).getValores();
            linhas = valores.size();
            colunas = valores.get(0).size();
        }
        
        
        if (colunas > 0 && linhas > 0) 
        {
            ItemDaListaParaMatriz item = new ItemDaListaParaMatriz(linhas, colunas, noTransferido);
            model.addElement(item);
            
            item.addListener(() -> { // recria a cache das alturas dos itens sempre que uma matriz ?? redimensionada
                recriaCacheDaAlturaDosItems();
            });
            
            if (programa != null)
            {
                programa.inspecionaMatriz(noTransferido.getIdParaInspecao(), linhas, colunas);
            }
            return true;
        }
        return false;
    }

    private boolean adicionaNoParametro(NoDeclaracaoParametro declaracaoParametro) 
    {
        ItemDaLista item = null;
        Quantificador quantificador = declaracaoParametro.getQuantificador();
        
        if (null == quantificador)
            return false;
        
        switch (quantificador) {
            case VALOR:
                item = new ItemDaListaParaVariavel(declaracaoParametro);
                break;
            case VETOR:
                item = new ItemDaListaParaVetor(declaracaoParametro);
                break;
            case MATRIZ:
                ItemDaListaParaMatriz itemMatriz = new ItemDaListaParaMatriz(declaracaoParametro);
                itemMatriz.addListener(() -> { recriaCacheDaAlturaDosItems(); });
                item = itemMatriz;
                break;
            default:
                break;
        }
        
        if (item != null) 
        {
            model.addElement(item);
            
            if (programa != null)
            {
                int idInspecao = item.getIdParaInspecao();
                if (item.ehVariavel())
                {
                    programa.inspecionaVariavel(idInspecao);
                }
                else if (item.ehVetor())
                {
                    ItemDaListaParaVetor itemVetor = (ItemDaListaParaVetor)item;
                    programa.inspecionaVetor(idInspecao, itemVetor.getColunas());
                }
                else // matriz
                {
                    ItemDaListaParaMatriz itemMatriz = (ItemDaListaParaMatriz)item;
                    int linhas = itemMatriz.getLinhas();
                    int colunas = itemMatriz.getColunas();
                    programa.inspecionaMatriz(idInspecao, linhas, colunas);
                }
            }
            
            return true;
        }
        
        return false;
    }

    public void adicionaNo(NoDeclaracaoBase noTransferido) throws ExcecaoVisitaASA 
    {
        boolean simboloInserido = false;
        if (noTransferido instanceof NoDeclaracaoVariavel) 
        {
            simboloInserido = adicionaNoVariavel((NoDeclaracaoVariavel) noTransferido);
        } 
        else if (noTransferido instanceof NoDeclaracaoParametro) 
        {
            simboloInserido = adicionaNoParametro((NoDeclaracaoParametro) noTransferido);
        } 
        else if (noTransferido instanceof NoDeclaracaoVetor) 
        {
            simboloInserido = adicionaNoVetor((NoDeclaracaoVetor) noTransferido);
        } 
        else if (noTransferido instanceof NoDeclaracaoMatriz) 
        {
            simboloInserido = adicionaNoMatriz((NoDeclaracaoMatriz) noTransferido);
        }
        if (simboloInserido) 
        {
            // altera o destaque do s??mbolo rec??m inserido
            model.get(model.getSize() - 1).setDesenhaDestaques(!programaExecutando);
            redesenhaItemsDaLista();
        }
    }

    private class TarefaReconstrucaoNosInspecionados implements Runnable 
    {

        @Override
        public void run() 
        {
            if (programa == null) {
                return;
            }
            
            try {

                LOGGER.log(Level.INFO, "Reconstruindo lista de n??s inspecionados");
                
                // verifica quais n??s devem ser mantidos no inspetor, os demais s??o apagados
                final List<NoDeclaracaoBase> nosQueSeraoMantidos = new ArrayList<>();

                programa.getArvoreSintaticaAbstrata().aceitar(new VisitanteNulo() {

                    @Override
                    public Object visitar(NoDeclaracaoVariavel noDeclaracaoVariavel) throws ExcecaoVisitaASA 
                    {
                        if (contemNo(noDeclaracaoVariavel)) {
                            nosQueSeraoMantidos.add(noDeclaracaoVariavel);
                        }
                        
                        return null;
                    }

                    @Override
                    public Object visitar(NoDeclaracaoParametro noDeclaracaoParametro) throws ExcecaoVisitaASA 
                    {
                        if (contemNo(noDeclaracaoParametro)) {
                            nosQueSeraoMantidos.add(noDeclaracaoParametro);
                        }
                        
                        return null;
                    }

                    @Override
                    public Object visitar(NoDeclaracaoMatriz noDeclaracaoMatriz) throws ExcecaoVisitaASA 
                    {
                        if (contemNo(noDeclaracaoMatriz)) {
                            nosQueSeraoMantidos.add(noDeclaracaoMatriz);
                        }
                        
                        return null;
                    }

                    @Override
                    public Object visitar(NoDeclaracaoVetor noDeclaracaoVetor) throws ExcecaoVisitaASA 
                    {
                        if (contemNo(noDeclaracaoVetor)) {
                            nosQueSeraoMantidos.add(noDeclaracaoVetor);
                        }
                        
                        return null;
                    }

                });
                
                model.clear();
                
                for (NoDeclaracaoBase no : nosQueSeraoMantidos) {
                    adicionaNo(no);
                }
                
            } catch (ExcecaoVisitaASA e) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    private boolean nosTemMesmoEscopo(NoDeclaracaoBase no1, NoDeclaracaoBase no2) 
    {
        String escopo1 = no1.getEscopo();
        String escopo2 = no2.getEscopo();
        
        return escopo1.equals(escopo2);
    }
    
    private boolean mesmoNo(NoDeclaracaoBase no1, NoDeclaracaoBase no2) 
    {
        boolean mesmoEscopo = nosTemMesmoEscopo(no1, no2);
        boolean mesmoNome = no1.getNome().equals(no2.getNome());
        boolean mesmoTipo = no1.getTipoDado() == no2.getTipoDado();
        
        return mesmoEscopo && mesmoNome && mesmoTipo;
    }

    public static void main(String args[]) 
    {
        WebLookAndFeel.install();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setVisible(true);
        frame.setLayout(new BorderLayout());

        final InspetorDeSimbolos inspetor = new InspetorDeSimbolos();
        inspetor.setBackground(ColorController.COR_CONSOLE);

        ItemDaListaParaVariavel itemVariavel = new ItemDaListaParaVariavel(new NoDeclaracaoVariavel("variavel", TipoDado.INTEIRO, false));
        itemVariavel.setValor(53);
        inspetor.model.addElement(itemVariavel);
        inspetor.model.addElement(new ItemDaListaParaVariavel(new NoDeclaracaoVariavel("outra vari??vel", TipoDado.LOGICO, false)));
        ItemDaListaParaVetor itemVetor = new ItemDaListaParaVetor(15, new NoDeclaracaoVetor("teste", TipoDado.INTEIRO, new NoInteiro(15), false));
        itemVetor.set(34, 12);
        itemVetor.set(34, 0);
        inspetor.model.addElement(itemVetor);

        inspetor.model.addElement(new ItemDaListaParaVetor(5, new NoDeclaracaoVetor("outro vetor", TipoDado.REAL, new NoInteiro(3), false)));

        ItemDaListaParaMatriz itemMatriz = new ItemDaListaParaMatriz(30, 30, new NoDeclaracaoMatriz("teste 2", TipoDado.INTEIRO, new NoInteiro(30), new NoInteiro(30), false));
        inspetor.model.addElement(itemMatriz);

        inspetor.model.addElement(new ItemDaListaParaMatriz(4, 4, new NoDeclaracaoMatriz("umNomeDeVari??vel bem grande", TipoDado.INTEIRO, new NoInteiro(4), new NoInteiro(4), false)));
        itemMatriz.set(345, 0, 1);
        
        inspetor.setPreferredSize(new Dimension(300, 600));

        inspetor.redesenhaItemsDaLista();

        frame.add(inspetor, BorderLayout.CENTER);

        JPanel panelBotoes = new JPanel();
        panelBotoes.setLayout(new BoxLayout(panelBotoes, BoxLayout.X_AXIS));
        
        JButton botaoAumentarFonte = new JButton(new AbstractAction("+") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                Font fonteNormal = RenderizadorBase.getFonte(RenderizadorBase.TipoFonte.NORMAL);
                inspetor.setTamanhoDaFonte(fonteNormal.getSize() + 2);
            }
        });
        
        JButton botaoDiminuirFonte = new JButton(new AbstractAction("-") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                Font fonteNormal = RenderizadorBase.getFonte(RenderizadorBase.TipoFonte.NORMAL);
                inspetor.setTamanhoDaFonte(fonteNormal.getSize() - 2);
            }
        });
        
        panelBotoes.add(botaoAumentarFonte);
        panelBotoes.add(botaoDiminuirFonte);
        botaoAumentarFonte.doClick();
        botaoAumentarFonte.doClick();
        frame.add(panelBotoes, BorderLayout.SOUTH);
        frame.pack();
    }
}
