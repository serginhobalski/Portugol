package br.univali.portugol.nucleo.analise.sintatica;

import java.io.IOException;
import org.antlr.v4.runtime.RecognitionException;
import br.univali.portugol.nucleo.analise.sintatica.antlr4.PortugolParser;
import br.univali.portugol.nucleo.asa.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;

import org.junit.Test;

/**
 *
 * @author Elieser
 */
public class GeradorASATest {

    @Test
    public void testMatrizComNumeroNegativo() throws Exception {

        PortugolParser parser = novoParser(
                " programa {                                                    "
                + "  inteiro m[][] = {{-1}}                                     "
                + "  funcao inicio(){                                           "
                + "  }                                                          "
                + "}                                                            "
        );

        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoMatriz declaracaoMatriz = (NoDeclaracaoMatriz) asa.getListaDeclaracoesGlobais().get(0);
        
        assertNoDeclaracaoMatriz(declaracaoMatriz, "m", 1, 1);
        
        Assert.assertTrue(declaracaoMatriz.temInicializacao());
                
        NoMatriz matriz = (NoMatriz) declaracaoMatriz.getInicializacao();
        
        Object valor = matriz.getValores().get(0).get(0);
        
        Assert.assertTrue(valor instanceof NoMenosUnario);
        
        Assert.assertTrue(((NoMenosUnario)valor).getExpressao() instanceof NoExpressaoLiteral);
        
        NoExpressaoLiteral<Integer> expressao = (NoExpressaoLiteral) ((NoMenosUnario)valor).getExpressao();
        
        Assert.assertEquals(new Integer(1), expressao.getValor());
    }
    
     @Test
    public void testOperacaoSendoUsadaComoParametro() throws Exception {

        PortugolParser parser = novoParser(
                " programa {                                                    "
                + "  funcao inicio(){                                           "
                + "         inteiro x = 0                                       "                        
                + "         teste(x += 2)                                       "
                + "  }                                                          "
                + "  funcao teste(inteiro x) {}                                 "
                + "}                                                            "
        );

       GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoFuncao inicio = getNoDeclaracaoFuncao("inicio", asa);
        
         assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(0), "x", TipoDado.INTEIRO, 0);
         
         NoChamadaFuncao teste = (NoChamadaFuncao)inicio.getBlocos().get(1);
         
         Assert.assertEquals(NoOperacaoAtribuicao.class.getName(), teste.getParametros().get(0).getClass().getName());
    }
    
    @Test
    public void testOperadoresBitwise() throws Exception {

        PortugolParser parser = novoParser(
                " programa {                                                    "
                + "  funcao inicio(){                                           "
                + "     inteiro x = ~1                                          "
                + "     x = x << 1                                              "
                + "     x = x >> 1                                              "
                + "     x = x & 1                                               "                        
                + "     x = x | 1                                               "
                + "     x = x ^ 1                                               "                                                
                + "  }                                                          "
                + "}                                                            "
        );

        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoFuncao inicio = getNoDeclaracaoFuncao("inicio", asa);
        
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(0), "x", TipoDado.INTEIRO);
        
        NoDeclaracaoVariavel declaracao = (NoDeclaracaoVariavel)inicio.getBlocos().get(0);
        Assert.assertTrue(declaracao.getInicializacao() instanceof NoBitwiseNao);
        
        Class operacoes[] = {
            NoOperacaoBitwiseLeftShift.class, 
            NoOperacaoBitwiseRightShift.class,
            NoOperacaoBitwiseE.class,
            NoOperacaoBitwiseOu.class,
            NoOperacaoBitwiseXOR.class
        };
        
        for (int i = 0; i < operacoes.length; i++) {
            NoOperacaoAtribuicao atribuicao = (NoOperacaoAtribuicao)inicio.getBlocos().get(1 + i);
        
            NoReferenciaVariavel x = (NoReferenciaVariavel)atribuicao.getOperandoEsquerdo();
            Assert.assertEquals("x", x.getNome());

            Assert.assertEquals(atribuicao.getOperandoDireito().getClass().getName(), operacoes[i].getName());
        }
    }
    
    @Test
    public void testReferenciaArray() throws Exception {

        PortugolParser parser = novoParser(
                " programa {                                                    "
                + "  funcao inicio(){                                           "
                + "     inteiro player2[] = {0}                                 "
                + "     player2[PONTUACAO]++                                    "
                + "     player2[PONTUACAO]--                                    "
                + "     ++player2[PONTUACAO]                                    "
                + "     --player2[PONTUACAO]                                    "
                + "  }                                                          "
                + "}                                                            "
        );

        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoFuncao inicio = getNoDeclaracaoFuncao("inicio", asa);
        
        List<NoBloco> blocos = inicio.getBlocos();
        
        assertNoDeclaracaoVetor((NoDeclaracaoVetor)blocos.get(0), "player2", new Object[]{0});
        
        for (int i = 0; i < 4; i++) {
            NoOperacaoAtribuicao atribuicao = (NoOperacaoAtribuicao)blocos.get(1 + i);
            Assert.assertEquals("player2", ((NoReferenciaVetor)atribuicao.getOperandoEsquerdo()).getNome());
        }
    }
    
    @Test
    public void testListaDeclaracaoMatrizes() throws Exception {

        PortugolParser parser = novoParser(
                " programa {                                                    "
                + "   real veiculos[10][6], galoes[][] = {{}}, pontos_reparo[3][8]   "
                + "}                                                            "
        );

        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        Assert.assertEquals("Erro no n??mero de declara????es globais", 3, asa.getListaDeclaracoesGlobais().size());
        
        List<NoDeclaracao> declaracoes = asa.getListaDeclaracoesGlobais();
        assertNoDeclaracaoMatriz((NoDeclaracaoMatriz)declaracoes.get(0), "veiculos", 10, 6);
        assertNoDeclaracaoMatriz((NoDeclaracaoMatriz)declaracoes.get(1), "galoes", new Object[][]{{}});//, 4, 2);
        assertNoDeclaracaoMatriz((NoDeclaracaoMatriz)declaracoes.get(2), "pontos_reparo", 3, 8);
    }
    
    @Test
    public void testXor() throws Exception {

        PortugolParser parser = novoParser(
                " programa {                                                    "
                + "      funcao inicio() {                                      "
                + "         inteiro x = 1                                       \n"
                + "         x = x ^ 2                                           \n"
                + "     }                                                       "
                + "}                                                            "
        );

        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoFuncao inicio = getNoDeclaracaoFuncao("inicio", asa);
        
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(0), "x", TipoDado.INTEIRO, 1);
        
        NoOperacaoAtribuicao atribuicao = (NoOperacaoAtribuicao)inicio.getBlocos().get(1);
        NoOperacaoBitwiseXOR xor = (NoOperacaoBitwiseXOR)atribuicao.getOperandoDireito();
        Assert.assertEquals("erro no xor", "x", ((NoReferenciaVariavel)xor.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("erro no operando direito ", new Integer(2), ((NoInteiro)xor.getOperandoDireito()).getValor());
    }
    
     @Test
    public void testListaDeclaracaoVetor() throws IOException, RecognitionException{
        PortugolParser parser = novoParser("programa {                      "
                + "     inteiro codewords_bloco1[78], codewords_bloco2[78], error_codewords_bloco1[20], error_codewords_bloco2[20]"
                + "}                                                        ");
        
         GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        Assert.assertEquals("Erro no n??mero de declara????es globais", 4, asa.getListaDeclaracoesGlobais().size());
    }
    
    @Test
    public void testAtribuicoesCompostas() throws Exception {

         PortugolParser parser = novoParser(
                " programa {                                                    "
                + "      funcao inicio() {                                      "
                + "         inteiro x = 1                                       \n"
                + "         x += 10                                             \n"
                + "         x -= 10                                             \n"
                + "         x *= 10                                             \n"
                + "         x /= 10                                             \n"                        
                + "         x += teste()                                        \n"                        
                + "     }                                                       \n"
                + "     funcao inteiro teste(){ retorne 1 }"
                + "}                                                            "
        );

        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoFuncao inicio = getNoDeclaracaoFuncao("inicio", asa);
        
        List<NoBloco> declaracoes = inicio.getBlocos();
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)declaracoes.get(0), "x", TipoDado.INTEIRO, 1);
        
        NoOperacaoSoma soma = (NoOperacaoSoma)((NoOperacaoAtribuicao)declaracoes.get(1)).getOperandoDireito();
        Assert.assertEquals("erro na atribui????o composta", "x", ((NoReferenciaVariavel)soma.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("erro no operando direito ", new Integer(10), ((NoInteiro)soma.getOperandoDireito()).getValor());
        
        NoOperacaoSubtracao subtracao = (NoOperacaoSubtracao)((NoOperacaoAtribuicao)declaracoes.get(2)).getOperandoDireito();;
        Assert.assertEquals("erro na atribui????o composta", "x", ((NoReferenciaVariavel)subtracao.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("erro no operando direito ", new Integer(10), ((NoInteiro)subtracao.getOperandoDireito()).getValor());
        
        NoOperacaoMultiplicacao multiplicacao = (NoOperacaoMultiplicacao)((NoOperacaoAtribuicao)declaracoes.get(3)).getOperandoDireito();
        Assert.assertEquals("erro na atribui????o composta", "x", ((NoReferenciaVariavel)multiplicacao.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("erro no operando direito ", new Integer(10), ((NoInteiro)multiplicacao.getOperandoDireito()).getValor());
        
        NoOperacaoDivisao divisao = (NoOperacaoDivisao)((NoOperacaoAtribuicao)declaracoes.get(4)).getOperandoDireito();;
        Assert.assertEquals("erro na atribui????o composta", "x", ((NoReferenciaVariavel)divisao.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("erro no operando direito ", new Integer(10), ((NoInteiro)divisao.getOperandoDireito()).getValor());
        
        //declaracoes. 
    }
    
    @Test
    public void testInteiroInicializadoComHexadecimal() throws Exception {

        PortugolParser parser = novoParser(
                "programa {                                                     "
                + "	inteiro x = 0xFaBcDe                                    \n"
                + "}                                                            "
        );

        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoVariavel no = (NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(0);
        assertNoDeclaracaoVariavel(no, "x", TipoDado.INTEIRO, 0xFaBcDe);
    }
    
    @Test
    public void testFuncaoRetornandoValor() throws Exception {

        PortugolParser parser = novoParser(
                "programa {                                                     "
                + "	funcao inteiro teste() {                                \n"
                + "         retorne 1                                           \n"
                + "     }                                                       \n"
                + "}                                                            "
        );

        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoFuncao teste = (NoDeclaracaoFuncao) asa.getListaDeclaracoesGlobais().get(0);
        NoRetorne retorne = (NoRetorne)teste.getBlocos().get(0);
        Assert.assertEquals((Integer)1, ((NoInteiro)retorne.getExpressao()).getValor());
    }  
    
    @Test
    public void testDeclaracaoMatrizVazia() throws Exception {

        PortugolParser parser = novoParser(
                "programa {                                                     "
                + "	inteiro x[] = {}                                        \n"
                + "	inteiro m[][] = {{}}                                    \n"
        //        + "	inteiro a[][] = {{}, {}}                               \n"
                + "}                                                            "
        );

        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoVetor x = (NoDeclaracaoVetor) asa.getListaDeclaracoesGlobais().get(0);
        NoDeclaracaoMatriz m = (NoDeclaracaoMatriz) asa.getListaDeclaracoesGlobais().get(1);
        
        assertNoDeclaracaoVetor(x, "x", new Object[]{});
        assertNoDeclaracaoMatriz(m, "m", new Object[][]{{}, {}});
    }
    
    @Test
    public void testDeclaracaoMatrizVetorComConstante() throws Exception {

        PortugolParser parser = novoParser(
                "programa {                                                     "
                + "	const inteiro T = 1000                                  \n"
                + "	inteiro matriz[T][T]                                    \n"
                + "	inteiro m[T]                                            \n"
                + "}                                                            "
        );

        Assert.assertEquals(0, parser.getNumberOfSyntaxErrors());
        
        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        
        NoDeclaracaoVariavel T = (NoDeclaracaoVariavel) asa.getListaDeclaracoesGlobais().get(0);
        NoDeclaracaoMatriz matriz = (NoDeclaracaoMatriz) asa.getListaDeclaracoesGlobais().get(1);
        NoDeclaracaoVetor m = (NoDeclaracaoVetor) asa.getListaDeclaracoesGlobais().get(2);
        
        assertNoDeclaracaoVariavel(T, "T", TipoDado.INTEIRO, 1000);
        Assert.assertTrue("deveria ser constante", T.constante());
        
        assertNoDeclaracaoVetor(m, "m", T);
        assertNoDeclaracaoMatriz(matriz, "matriz", T, T);
    }
    
    @Test
    public void testParaComVariasVariaveisDeclaradas() throws Exception {

        PortugolParser parser = novoParser(
                "programa {                                                     "
                + "	funcao inicio() {                                       "
                + "		para(inteiro i = 0, j = 0, k = 5,l; i < 5; i++){"
                + "			para(j = 1; j < 5; i++){                "
                + "			}                                       "
                + "		}                                               "
                + "	}                                                       "
                + "}                                                            "
        );

        Assert.assertEquals(0, parser.getNumberOfSyntaxErrors());

        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoFuncao inicio = getNoDeclaracaoFuncao("inicio", asa);
        
        NoPara para = (NoPara) inicio.getBlocos().get(0);
        Assert.assertEquals("Erro no n??mero de inicializa????es", 4,  para.getInicializacoes().size());
        
        List<NoBloco> inicializacoes = para.getInicializacoes();
        
        // para(inteiro i = 0, j = 0, k = 5,l; i < 5; i++)
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicializacoes.get(0), "i", TipoDado.INTEIRO, 0);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicializacoes.get(1), "j", TipoDado.INTEIRO, 0);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicializacoes.get(2), "k", TipoDado.INTEIRO, 5);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicializacoes.get(3), "l", TipoDado.INTEIRO);
        
        // para(j = 1; j < 5; i++){
        NoPara paraAninhado = (NoPara)para.getBlocos().get(0);
        List<NoBloco> inicializacoesParaAninhado = paraAninhado.getInicializacoes();
        Assert.assertEquals("Erro no n??mero de inicializa????es do para aninhado", 1, inicializacoesParaAninhado.size());
        
        NoOperacaoAtribuicao atribuicao = (NoOperacaoAtribuicao)inicializacoesParaAninhado.get(0);
        Assert.assertEquals("Erro no nome da vari??vel", "j", ((NoReferenciaVariavel)atribuicao.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("Erro na inicializa????o da vari??vel j", new Integer(1), ((NoInteiro)atribuicao.getOperandoDireito()).getValor());
    }
    
    @Test
    public void testFuncaoLeiaComVariasVariaveis() throws IOException, ExcecaoVisitaASA {

        PortugolParser parser = novoParser("programa {                          "
                + " funcao inicio(){            "
                + "   inteiro a,b,c                 "
                + "   leia(a,b,c)                   "
                + " }                           "
                + "}                                                          ");

        Assert.assertEquals(0, parser.getNumberOfSyntaxErrors());
        
        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        NoDeclaracaoFuncao inicio = getNoDeclaracaoFuncao("inicio", asa);
        
        // a lista de declara????o de vari??veis ?? quebrada em declara????es separadas
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(0), "a", TipoDado.INTEIRO);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(1), "b", TipoDado.INTEIRO);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(2), "c", TipoDado.INTEIRO);
    }
    
    @Test
    public void testListaDeclaracaoVariaveis() throws IOException, RecognitionException{
        PortugolParser parser = novoParser(""
                + "programa {                                                               "
                + "     inteiro base_x, base_y, espaco, cor_base=0, iterador=360, portugol=0"
                + "     cadeia c                                                            "
                + "}                                                                        "
        );

        Assert.assertEquals(0, parser.getNumberOfSyntaxErrors());
        
        GeradorASA geradorASA = new GeradorASA(parser);
        ASA asa = geradorASA.geraASA();
        
        Assert.assertEquals("a lista de declara????es deveria ter 7 vari??veis", 7, asa.getListaDeclaracoesGlobais().size());
        
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(0), "base_x", TipoDado.INTEIRO);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(1), "base_y", TipoDado.INTEIRO);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(2), "espaco", TipoDado.INTEIRO);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(3), "cor_base", TipoDado.INTEIRO, 0);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(4), "iterador", TipoDado.INTEIRO, 360);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(5), "portugol", TipoDado.INTEIRO, 0);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(6), "c", TipoDado.CADEIA);
    }
    
    
    @Test
    public void testDeclaracaoFuncoes() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {                      "
                + " funcao inicio() {                                       "
                + " }                                                       "
                + " funcao teste(inteiro x, real teste[]) {                   "
                + " }                                                       "
                + " funcao real outra(logico &x, inteiro x, cadeia teste[][]) { "
                + "     retorne 1.0                                         "
                + " }                                                       "
                + "}                                                        ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();
        
        NoDeclaracaoFuncao inicio = getNoDeclaracaoFuncao("inicio", asa);
        
        Assert.assertTrue("a fun????o in??cio n??o tem filhos", inicio.getBlocos().isEmpty());
        Assert.assertTrue("a fun????o in??cio n??o tem par??metros", inicio.getParametros().isEmpty());
        
        NoDeclaracaoFuncao teste = getNoDeclaracaoFuncao("teste", asa);
        Assert.assertTrue("a fun????o teste n??o tem filhos", teste.getBlocos().isEmpty());
        Assert.assertEquals("a fun????o teste tem 2 par??metros", 2, teste.getParametros().size());
        
        List<NoDeclaracaoParametro> parametrosTeste = teste.getParametros();
        assertNoDeclaracaoParametro(parametrosTeste.get(0), "x", TipoDado.INTEIRO, ModoAcesso.POR_VALOR, Quantificador.VALOR);
        assertNoDeclaracaoParametro(parametrosTeste.get(1), "teste", TipoDado.REAL, ModoAcesso.POR_VALOR, Quantificador.VETOR);
        
        NoDeclaracaoFuncao outra = getNoDeclaracaoFuncao("outra", asa);
        Assert.assertEquals("a fun????o outra tem 1 filho", 1, outra.getBlocos().size());
        Assert.assertEquals("a fun????o outra tem 3 par??metros", 3, outra.getParametros().size());
        Assert.assertEquals("A fun????o outra retorna um real", TipoDado.REAL, outra.getTipoDado());
        
        List<NoDeclaracaoParametro> parametrosOutra = outra.getParametros();
        assertNoDeclaracaoParametro(parametrosOutra.get(0), "x", TipoDado.LOGICO, ModoAcesso.POR_REFERENCIA, Quantificador.VALOR);
        assertNoDeclaracaoParametro(parametrosOutra.get(1), "x", TipoDado.INTEIRO, ModoAcesso.POR_VALOR, Quantificador.VALOR);
        assertNoDeclaracaoParametro(parametrosOutra.get(2), "teste", TipoDado.CADEIA, ModoAcesso.POR_VALOR, Quantificador.MATRIZ);
    }    
    
    @Test
    public void testVariaveisLocais() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa { "
                + " funcao inicio() {                               "
                + "     inteiro x = +1                              "
                + "     real a = 10.0                               "
                + "     cadeia teste = \"teste\"                    "
                + "     cadeia concat = \"conca\" + \"tena????o\"     "
                + "     caracter c = 'a'                            "
                + "     logico l = verdadeiro                       "
                + "     inteiro soma = -(10 + 2)                    "
                + "     inteiro soma2 = 10 + 2 * x / a              "
                + "     inteiro vetor[3]                            "
                + "     inteiro v[] = {1, 2, 3, 10}                 "
                + "     inteiro m[3][3]                             "
                + "     inteiro matriz[][] = {{1, 2}, {10, 3}}      "
                + "     inteiro i = 1 << 1                          "
                + "     inteiro j = 1 >> 1                          "
                + " }                                               "
                + "}                                                ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();
        
        NoDeclaracaoFuncao inicio = getNoDeclaracaoFuncao("inicio", asa);
        
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(0), "x", TipoDado.INTEIRO, 1);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(1), "a", TipoDado.REAL, 10.0);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(2), "teste", TipoDado.CADEIA, "teste");
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(3), "concat", TipoDado.CADEIA, NoOperacaoSoma.class);// new NoOperacaoSoma(null, null));
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(4), "c", TipoDado.CARACTER, 'a');
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(5), "l", TipoDado.LOGICO, true);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(6), "soma", TipoDado.INTEIRO, NoMenosUnario.class);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(7), "soma2", TipoDado.INTEIRO, NoOperacaoSoma.class);
        assertNoDeclaracaoVetor((NoDeclaracaoVetor)inicio.getBlocos().get(8), "vetor", 3);
        assertNoDeclaracaoVetor((NoDeclaracaoVetor)inicio.getBlocos().get(9), "v", new Object[]{1, 2, 3, 10});
        assertNoDeclaracaoMatriz((NoDeclaracaoMatriz)inicio.getBlocos().get(10), "m", 3, 3);
        assertNoDeclaracaoMatriz((NoDeclaracaoMatriz)inicio.getBlocos().get(11), "matriz", new Integer[][]{{1, 2}, {10, 3}});
        
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(12), "i", TipoDado.INTEIRO, NoOperacaoBitwiseLeftShift.class);
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)inicio.getBlocos().get(13), "j", TipoDado.INTEIRO, NoOperacaoBitwiseRightShift.class);
    }
    
    @Test
    public void testSe() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {      "
                + " funcao inicio() {                       "
                + "     se (x > 10)  {                      "
                + "         inteiro a = 10                  "
                + "         escreva(x)                      "
                + "     }                                   "
                + "     se (x < 5) {                        "
                + "         escreva(\"teste\")              "
                + "         escreva(\"teste\", x)           "
                + "     }                                   "
                + "     se (x > 10)                         "
                + "         escreva(x)                      "
                + "     senao {                             "
                + "         se(x > 12) {}                   "
                + "         senao { escreva (\"teste\", x) }"
                + "     }                                   "
                + " }                                       " // func??o in??cio
                + "}                                        ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();
        
        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        
        NoSe se = (NoSe) funcaoInicio.getBlocos().get(0);
        Assert.assertTrue("a condi????o do primeiro se ?? do tipo >", se.getCondicao() instanceof NoOperacaoLogicaMaior);
        NoOperacaoLogicaMaior condicao = (NoOperacaoLogicaMaior)se.getCondicao();
        Assert.assertEquals("o operando esquerdo da condi????o deveria ser 'x'", "x", ((NoReferenciaVariavel)condicao.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("o operando direito da condi????o deveria ser 10", (Integer)10, ((NoInteiro)condicao.getOperandoDireito()).getValor());
        Assert.assertEquals("o primeiro se deveria ter 2 filhos no bloco verdadeiro", 2, se.getBlocosVerdadeiros().size());
        Assert.assertTrue("o primeiro se n??o tem um sen??o", se.getBlocosFalsos() == null);
        
        NoSe se2 = (NoSe) funcaoInicio.getBlocos().get(1);
        Assert.assertTrue("a condi????o do segundo se ?? do tipo <", se2.getCondicao() instanceof NoOperacaoLogicaMenor);
        NoOperacaoLogicaMenor condicao2 = (NoOperacaoLogicaMenor)se2.getCondicao();
        Assert.assertEquals("o operando esquerdo da condi????o deveria ser 'x'", "x", ((NoReferenciaVariavel)condicao2.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("o operando direito da condi????o deveria ser 5", (Integer)5, ((NoInteiro)condicao2.getOperandoDireito()).getValor());
        Assert.assertEquals("o segundo se deveria ter 2 filhos no bloco verdadeiro", 2, se2.getBlocosVerdadeiros().size());
        Assert.assertTrue("o segundo se n??o tem sen??o", se2.getBlocosFalsos() == null);
        
        NoSe se3 = (NoSe) funcaoInicio.getBlocos().get(2);
        Assert.assertTrue("a condi????o do terceiro se ?? do tipo >", se3.getCondicao() instanceof NoOperacaoLogicaMaior);
        NoOperacaoLogicaMaior condicao3 = (NoOperacaoLogicaMaior)se3.getCondicao();
        Assert.assertEquals("o operando esquerdo da condi????o deveria ser 'x'", "x", ((NoReferenciaVariavel)condicao3.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("o operando direito da condi????o deveria ser 10", (Integer)10, ((NoInteiro)condicao3.getOperandoDireito()).getValor());
        Assert.assertEquals("o terceiro se deveria ter 1 filho no bloco verdadeiro", 1, se3.getBlocosVerdadeiros().size());
        Assert.assertEquals("o terceiro se deveria ter 1 filho no bloco falso", 1, se3.getBlocosFalsos().size());
        
        NoSe seAninhado = (NoSe) se3.getBlocosFalsos().get(0);
        Assert.assertTrue("a condi????o do se aninhado ?? do tipo >", seAninhado.getCondicao() instanceof NoOperacaoLogicaMaior);
        NoOperacaoLogicaMaior condicao4 = (NoOperacaoLogicaMaior)seAninhado.getCondicao();
        Assert.assertEquals("o operando esquerdo da condi????o deveria ser 'x'", "x", ((NoReferenciaVariavel)condicao4.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("o operando direito da condi????o deveria ser 12", (Integer)12, ((NoInteiro)condicao4.getOperandoDireito()).getValor());
        Assert.assertEquals("o se aninhado deveria ter 0 filhos no bloco verdadeiro", 0, seAninhado.getBlocosVerdadeiros().size());
        Assert.assertEquals("o se aninhado deveria ter 1 filho no bloco falso", 1, seAninhado.getBlocosFalsos().size());
    }
    
    @Test
    public void testEnquanto() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {  "
                + " funcao inicio() {                   "
                + "     enquanto (x > 10)  {            "
                + "         se (x < 5) {                "
                + "             escreva(\"teste\")      "
                + "         }                           "
                + "     }                               "
                + "     enquanto (x > 10)               "
                + "         escreva(x)                  "
                + " }                                   " // func??o in??cio
                + "}                                    ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();
        
        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        
        NoEnquanto enquanto = (NoEnquanto) funcaoInicio.getBlocos().get(0);
        
        Assert.assertTrue("a condi????o do primeiro enquanto ?? do tipo >", enquanto.getCondicao() instanceof NoOperacaoLogicaMaior);
        NoOperacaoLogicaMaior condicao = (NoOperacaoLogicaMaior)enquanto.getCondicao();
        Assert.assertEquals("o operando esquerdo da condi????o deveria ser 'x'", "x", ((NoReferenciaVariavel)condicao.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("o operando direito da condi????o deveria ser 10", (Integer)10, ((NoInteiro)condicao.getOperandoDireito()).getValor());
        Assert.assertEquals("o primeiro enquanto deveria ter um filho", 1, enquanto.getBlocos().size());
        
        NoEnquanto enquanto2 = (NoEnquanto) funcaoInicio.getBlocos().get(1);
        
        Assert.assertTrue("a condi????o do segundo enquanto ?? do tipo <", enquanto2.getCondicao() instanceof NoOperacaoLogicaMaior);
        NoOperacaoLogicaMaior condicao2 = (NoOperacaoLogicaMaior)enquanto2.getCondicao();
        Assert.assertEquals("o operando esquerdo da condi????o deveria ser 'x'", "x", ((NoReferenciaVariavel)condicao2.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("o operando direito da condi????o deveria ser 10", (Integer)10, ((NoInteiro)condicao2.getOperandoDireito()).getValor());
        Assert.assertEquals("o segundo enquanto deveria ter um filho", 1, enquanto2.getBlocos().size());
    }
    
    @Test
    public void testFacaEnquanto() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {  "
                + " funcao inicio() {                   "
                + "     inteiro x                       "
                + "     faca {                          "
                + "         se (x < 5) {                "
                + "             escreva()               "
                + "             escreva(\"teste\")      "
                + "             escreva(\"teste\", x)   "
                + "         }                           "
                + "     }                               "
                + "     enquanto (x > 10)               "
                + " }                                   "
                + "}                                    ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();
        
        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        
        NoDeclaracaoVariavel x = (NoDeclaracaoVariavel) funcaoInicio.getBlocos().get(0);
        assertNoDeclaracaoVariavel(x, "x", TipoDado.INTEIRO);
        
        NoFacaEnquanto facaEnquanto = (NoFacaEnquanto) funcaoInicio.getBlocos().get(1);
        Assert.assertTrue("condi????o do fa??a enquanto ?? uma opera????o de maior", facaEnquanto.getCondicao() instanceof NoOperacaoLogicaMaior);
        
        NoOperacaoLogicaMaior condicaoFacaEnquanto = (NoOperacaoLogicaMaior) facaEnquanto.getCondicao();
        Assert.assertEquals("operador esquerdo da condi????o ?? x", "x", ((NoReferenciaVariavel)condicaoFacaEnquanto.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("operador direito da condi????o ?? 10", new Integer(10), ((NoInteiro)condicaoFacaEnquanto.getOperandoDireito()).getValor());
        
        Assert.assertEquals("o fa??a enquanto deveria ter 1 comando filho (o SE)", 1, facaEnquanto.getBlocos().size());
        
    }
    
    @Test
    public void testEscolhaCaso() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {  "
                + " funcao inicio() {                   "
                + "     inteiro x = 1                   "
                + "     escolha (x) {                   "
                + "         caso 1:                     "
                + "             escreva(x)              "
                + "             escreva(x + 1)          "                
                + "             pare                    "
                + "         caso 2:                     "
                + "             escreva(x+1)            "
                + "         caso contrario:             "
                + "             escreva(\"asd\")        "
                + "     }                               "
                + " }                                   " // func??o in??cio
                + "}                                    ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();
        
        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)funcaoInicio.getBlocos().get(0), "x", TipoDado.INTEIRO, 1);
        
        NoEscolha noEscolha = (NoEscolha) funcaoInicio.getBlocos().get(1);
        Assert.assertEquals("vari??vel do escolha deveria ser x", "x", ((NoReferenciaVariavel)noEscolha.getExpressao()).getNome());
        Assert.assertEquals("escolha deveria ter 3 casos", 3, noEscolha.getCasos().size());
        
        NoCaso caso1 = noEscolha.getCasos().get(0);
        Assert.assertEquals("o caso 1 deveria ter o valor 1 como express??o", new Integer(1), ((NoInteiro)caso1.getExpressao()).getValor());
        Assert.assertEquals("O caso 1 deveria ter 3 comandos filhos, incluindo o 'pare'", 3, caso1.getBlocos().size());
        
        assertNoChamadaFuncao((NoChamadaFuncao)caso1.getBlocos().get(0), "escreva", new Object[]{new NoReferenciaVariavel(null, null)});
        assertNoChamadaFuncao((NoChamadaFuncao)caso1.getBlocos().get(1), "escreva", new Object[]{new NoOperacaoSoma(null, null)});
        
        Assert.assertTrue("o caso 1 cont??m o comando pare no final", caso1.getBlocos().get(2) instanceof NoPare);
        
        NoCaso caso2 = noEscolha.getCasos().get(1);
        Assert.assertEquals("o caso 2 deveria ter o valor 2 como express??o", new Integer(2), ((NoInteiro)caso2.getExpressao()).getValor());
        Assert.assertEquals("O caso 2 deveria ter 1 comando filho", 1, caso2.getBlocos().size());
        assertNoChamadaFuncao((NoChamadaFuncao)caso2.getBlocos().get(0), "escreva", new Object[]{new NoOperacaoSoma(null, null)});
        
        NoCaso casoContrario = noEscolha.getCasos().get(2);
        Assert.assertEquals("O caso contr??rio deveria ter 1 comando filho", 1, casoContrario.getBlocos().size());
        assertNoChamadaFuncao((NoChamadaFuncao)casoContrario.getBlocos().get(0), "escreva", new Object[]{"asd"});
    }
    
    @Test
    public void testChamadasFuncoes() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {                          "
                + " inteiro x = 1                                               "
                + "                                                             "
                + " funcao inicio() {                                           "
                + "     teste(10, 12.0/x)                                       "
                + "     logico b = falso                                        "
                + "     cadeia frases[2]                                        "
                + "     real m[2][2]                                            "
                + "     real y = outra(b, frases)                               "
                + " }                                                           "
                + " funcao teste(inteiro x, real teste) {                       "
                + " }                                                           "
                + " funcao real outra(logico &x, cadeia teste[], real m[][]) {  "
                + "     retorne 0.0                                             "
                + " }                                                           "
                + "}                                                            ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();
        
        Assert.assertEquals("o n??mero de declara????es globais deveria ser 4 (uma vari??vel e 3 fun????es)", 4, asa.getListaDeclaracoesGlobais().size());
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(0), "x", TipoDado.INTEIRO, 1);
        
        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        
        NoChamadaFuncao chamadaTeste = (NoChamadaFuncao) funcaoInicio.getBlocos().get(0);
        assertNoChamadaFuncao(chamadaTeste, "teste", new Object[]{10, new NoOperacaoDivisao(null, null)});
        
        NoDeclaracaoVariavel declaracaoB = (NoDeclaracaoVariavel) funcaoInicio.getBlocos().get(1);
        assertNoDeclaracaoVariavel(declaracaoB, "b", TipoDado.LOGICO, false);
        
        NoDeclaracaoVetor frases = (NoDeclaracaoVetor) funcaoInicio.getBlocos().get(2);
        assertNoDeclaracaoVetor(frases, "frases", 2);
        
        NoDeclaracaoMatriz matrizM = (NoDeclaracaoMatriz) funcaoInicio.getBlocos().get(3);
        assertNoDeclaracaoMatriz(matrizM, "m", 2, 2);
        
        NoDeclaracaoVariavel y = (NoDeclaracaoVariavel) funcaoInicio.getBlocos().get(4);
        assertNoDeclaracaoVariavel(y, "y", TipoDado.REAL);
        
        NoChamadaFuncao chamadaFuncaoOutra = (NoChamadaFuncao) y.getInicializacao();
        assertNoChamadaFuncao(chamadaFuncaoOutra, "outra", new Object[]{new NoReferenciaVariavel(null, null), new NoReferenciaVetor(null, null, null)});
    }
    
    @Test
    public void testComentariosUnicaLinha() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {                          "
                + " inteiro x = 1 // coment??rio na vari??vel                   \n"
                + "                                                             "
                + " funcao inicio() {                                           "
                + "     teste(10, 12.0/x)                                       "
                + "     // logico b = falso                                   \n"
                + " }                                                           "
                + " funcao teste(inteiro x, real teste) {                       "
                + " } // coment??rio depois da fun????o                          \n"
                + " funcao real outra(logico &x, cadeia teste[], real m[][]) {  "
                + " }                                                           "
                + "} // comentario no fim do programa                         \n");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();
        
        Assert.assertEquals("o n??mero de declara????es globais deveria ser 4 (uma vari??vel e 3 fun????es)", 4, asa.getListaDeclaracoesGlobais().size());
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(0), "x", TipoDado.INTEIRO, 1);
        
        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        Assert.assertTrue("fun????o in??cio deveria ter um filho que ?? uma chamada pra fun????o", funcaoInicio.getBlocos().get(0) instanceof  NoChamadaFuncao);
        
        NoDeclaracaoFuncao funcaoTeste = getNoDeclaracaoFuncao("teste", asa);
        Assert.assertTrue("fun????o 'teste' n??o deveria ter filhos", funcaoTeste.getBlocos().isEmpty());
        
        NoDeclaracaoFuncao funcaoOutra = getNoDeclaracaoFuncao("outra", asa);
        Assert.assertTrue("fun????o 'outra' n??o deveria ter filhos", funcaoOutra.getBlocos().isEmpty());
    }
    
    @Test
    public void testComentariosMultiLinha() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {                          "
                + "     inteiro x = 1                                           "
                + "     /* testando um coment??rio                               "
                + "         com v??rias linhas para                              "
                + "         para ver se funciona corretamente                   "
                + "     */                                                      "
                + "                                                             "
                + "     /* comentando antes da fun????o */                        "
                + "     funcao inicio() {                                       "
                + "         /* comentando dentro da fun????o */                   "
                + "     }                                                       "
                + "} // comentario no fim do programa                           ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();
        
        Assert.assertEquals("o n??mero de declara????es globais deveria ser 2 (uma vari??vel e a fun????o in??cio)", 2, asa.getListaDeclaracoesGlobais().size());
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)asa.getListaDeclaracoesGlobais().get(0), "x", TipoDado.INTEIRO, 1);
        
        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        Assert.assertTrue("fun????o in??cio n??o deveria ter filhos", funcaoInicio.getBlocos().isEmpty());
    }
    
    @Test
    public void testBibliotecasNativas() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {                          "
                + "     inclua biblioteca Graficos                              "
                + "     inclua biblioteca Sons --> s                             "
                + "                                                             "
                + "     funcao inicio() {                                       "
                + "         Graficos.carregar_som(\"teste\")"
                + "     }                                                       "
                + "}                                                            ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();
        
        List<NoInclusaoBiblioteca> inclusoes = asa.getListaInclusoesBibliotecas();
        assertNoInclusaoBiblioteca(inclusoes.get(0), "Graficos");
        assertNoInclusaoBiblioteca(inclusoes.get(1), "Sons", "s");
        
        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        NoChamadaFuncao chamadaFuncao = (NoChamadaFuncao) funcaoInicio.getBlocos().get(0);
        assertNoChamadaFuncao(chamadaFuncao, "carregar_som", "Graficos", new String[]{"teste"});
    }
    
    @Test
    public void testAtribui????es() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {                          "
                + "     inclua biblioteca Graficos                              "
                + "                                                             "
                + "     funcao inicio() {                                       "
                + "         inteiro som = 0                                     "
                + "         som =   som + 1                                     "
                + "         inteiro teste = som                                 "
                + "         som = teste                                         "
                + "         inteiro som = Graficos.carregar_som(\"teste\")      "
                + "     }                                                       "
                + "}                                                            ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();

        Assert.assertEquals("O programa deveria ter 2 declara????es globais (a inclus??o da biblioteca e 'inicio')", 2, asa.getListaDeclaracoesGlobais().size());
        
        assertNoInclusaoBiblioteca(asa.getListaInclusoesBibliotecas().get(0), "Graficos");
        
        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        
        // inteiro som = 0 
        NoDeclaracaoVariavel inteiroSom = (NoDeclaracaoVariavel) funcaoInicio.getBlocos().get(0);
        assertNoDeclaracaoVariavel(inteiroSom, "som", TipoDado.INTEIRO, 0); // testa se est?? inicializada com zero
        
        // som =   som + 1
        NoOperacaoAtribuicao atribuicao = (NoOperacaoAtribuicao) funcaoInicio.getBlocos().get(1);
        Assert.assertEquals("A variavel deveria ser 'som'", "som", ((NoReferenciaVariavel)atribuicao.getOperandoEsquerdo()).getNome());
        Assert.assertTrue("A variavel recebe o resultaod de uma soma", atribuicao.getOperandoDireito() instanceof NoOperacaoSoma);
        
        NoReferenciaVariavel refVariavel = (NoReferenciaVariavel) ((NoOperacaoSoma)atribuicao.getOperandoDireito()).getOperandoEsquerdo();
        NoInteiro noInteiro = (NoInteiro) ((NoOperacaoSoma)atribuicao.getOperandoDireito()).getOperandoDireito();
        Assert.assertTrue("soma + 1", refVariavel.getNome().equals("som")  && noInteiro.getValor() == 1);
        
        // inteiro teste = som 
        assertNoDeclaracaoVariavel((NoDeclaracaoVariavel)funcaoInicio.getBlocos().get(2), "teste", TipoDado.INTEIRO);
        
        // som = teste
        NoOperacaoAtribuicao atribuicao2 = (NoOperacaoAtribuicao) funcaoInicio.getBlocos().get(3);
        Assert.assertEquals("A variavel que recebe o valor chama-se 'som'", "som", ((NoReferenciaVariavel)atribuicao2.getOperandoEsquerdo()).getNome());
        Assert.assertTrue("A variavel recebe o valor de outra vari??vel", atribuicao2.getOperandoDireito() instanceof NoReferenciaVariavel);
        Assert.assertEquals("A variavel recebe o valor da variavel 'teste'", "teste", ((NoReferenciaVariavel)atribuicao2.getOperandoDireito()).getNome());
        
        // inteiro som = Graficos.carregar_som(\"teste\")
        NoDeclaracaoVariavel declaracao2 = (NoDeclaracaoVariavel) funcaoInicio.getBlocos().get(4);
        Assert.assertEquals("A variavel que recebe o valor chama-se 'som'", "som", declaracao2.getNome());
        Assert.assertTrue("A variavel recebe o valor retornado por uma fun????o", declaracao2.getInicializacao() instanceof NoChamadaFuncao);
        assertNoChamadaFuncao((NoChamadaFuncao)declaracao2.getInicializacao(), "carregar_som", "Graficos", new String[]{"teste"});
    }
    
    @Test
    public void testExpressoesComArrays() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {                          "
                + "     inteiro v[] = {1, 2, 3}                                 "
                + "     inteiro m[2][2]                                         "
                + "                                                             "
                + "     funcao inicio() {                                       "
                + "         inteiro som = v[0]                                  "
                + "         som =   m[1][2]                                     "
                + "         som =   m[10/2][V[0]]                               "
                + "         m[0][1] = m[0][0]                                   "
                + "         m[v[0]][v[1]] = m[m[0][0]][v[0]]                                   "
                + "     }                                                       "
                + "}                                                            ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();

        Assert.assertEquals("O programa deveria ter 3 declara????es globais, 1 vetor, 1 matriz e uma fun????o", 3, asa.getListaDeclaracoesGlobais().size());
        
        NoDeclaracaoVetor noVetor = (NoDeclaracaoVetor)asa.getListaDeclaracoesGlobais().get(0);
        assertNoDeclaracaoVetor(noVetor, "v", new Integer[]{1, 2, 3});
        
        NoDeclaracaoMatriz noMatriz = (NoDeclaracaoMatriz)asa.getListaDeclaracoesGlobais().get(1);
        assertNoDeclaracaoMatriz(noMatriz, "m", 2, 2);
        
        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        
        Assert.assertEquals("A fun????o in??cio tem 5 n??s filhos", 5, funcaoInicio.getBlocos().size());
        
        // testa a linha 'inteiro som = v[0]'
        NoDeclaracaoVariavel declaracaoInteiroSom = (NoDeclaracaoVariavel)funcaoInicio.getBlocos().get(0);
        Assert.assertEquals("O nome da vari??vel deveria ser 'som'", "som", declaracaoInteiroSom.getNome());
        Assert.assertEquals("O tipo da vari??vel 'som' deveria ser 'inteiro'", TipoDado.INTEIRO, declaracaoInteiroSom.getTipoDado());
        Assert.assertEquals("A vari??vel 'som' est?? inicializada", true, declaracaoInteiroSom.temInicializacao());
        
        Assert.assertTrue("A vari??vel 'som' est?? inicializada com uma refer??ncia para vetor", declaracaoInteiroSom.getInicializacao() instanceof NoReferenciaVetor);
        Assert.assertEquals("A vari??vel 'som' est?? inicializada com uma refer??ncia para o vetor 'v'", "v", ((NoReferenciaVetor)declaracaoInteiroSom.getInicializacao()).getNome());
        Assert.assertEquals("A vari??vel 'som' est?? inicializada com uma refer??ncia para o vetor 'v' no ??ndice [0]'", new Integer(0), ((NoInteiro)((NoReferenciaVetor)declaracaoInteiroSom.getInicializacao()).getIndice()).getValor());
        
        // testa a linha 'som =   m[1][2]'
        NoOperacaoAtribuicao atribuicaoSom = (NoOperacaoAtribuicao)funcaoInicio.getBlocos().get(1);
        Assert.assertEquals("A vari??vel 'som' est?? recebendo um valor", "som", ((NoReferenciaVariavel)atribuicaoSom.getOperandoEsquerdo()).getNome());
        Assert.assertTrue("A vari??vel 'som' est?? recebendo um valor que ?? uma refer??ncia para matriz", atribuicaoSom.getOperandoDireito() instanceof NoReferenciaMatriz);
        Assert.assertEquals("A vari??vel 'som' est?? recebendo um valor que ?? uma refer??ncia para a matriz 'm'", "m", ((NoReferenciaMatriz)atribuicaoSom.getOperandoDireito()).getNome());
        Assert.assertEquals("A vari??vel 'som' est?? recebendo um valor que ?? uma refer??ncia para a matriz 'm' na linha 1", new Integer(1), ((NoInteiro)((NoReferenciaMatriz)atribuicaoSom.getOperandoDireito()).getLinha()).getValor());
        Assert.assertEquals("A vari??vel 'som' est?? recebendo um valor que ?? uma refer??ncia para a matriz 'm' na coluna 2", new Integer(2), ((NoInteiro)((NoReferenciaMatriz)atribuicaoSom.getOperandoDireito()).getColuna()).getValor());
        
        // testando a linha 'som =   m[10/2][V[0]]'
        NoOperacaoAtribuicao atribuicaoSom2 = (NoOperacaoAtribuicao)funcaoInicio.getBlocos().get(2);
        Assert.assertEquals("A vari??vel 'som' est?? recebendo um valor", "som", ((NoReferenciaVariavel)atribuicaoSom2.getOperandoEsquerdo()).getNome());
        Assert.assertTrue("A vari??vel 'som' est?? recebendo um valor que ?? uma refer??ncia para matriz", atribuicaoSom2.getOperandoDireito() instanceof NoReferenciaMatriz);
        Assert.assertEquals("A vari??vel 'som' est?? recebendo um valor que ?? uma refer??ncia para a matriz 'm'", "m", ((NoReferenciaMatriz)atribuicaoSom2.getOperandoDireito()).getNome());
        Assert.assertTrue("'som' recebe o valor da matriz 'm' no ??ndice de linha que ?? o resultado de uma divis??o", ((NoReferenciaMatriz)atribuicaoSom2.getOperandoDireito()).getLinha() instanceof NoOperacaoDivisao);
        Assert.assertTrue("'som' recebe o valor da matriz 'm' no ??ndice de coluna que ?? uma refer??ncia para um vetor", ((NoReferenciaMatriz)atribuicaoSom2.getOperandoDireito()).getColuna()instanceof NoReferenciaVetor);
        Assert.assertEquals("'som' recebe o valor da matriz 'm' no ??ndice de coluna que ?? uma refer??ncia para um vetor na posi????o [0]", new Integer(0), ((NoInteiro)((NoReferenciaVetor)((NoReferenciaMatriz)atribuicaoSom2.getOperandoDireito()).getColuna()).getIndice()).getValor());
    
        // testando a linha 'm[0][1] = m[0][0]'
        NoOperacaoAtribuicao atribuicaoM = (NoOperacaoAtribuicao)funcaoInicio.getBlocos().get(3);
        Assert.assertEquals("A matriz 'm' est?? recebendo um valor", "m", ((NoReferenciaMatriz)atribuicaoM.getOperandoEsquerdo()).getNome());
        Assert.assertEquals("A matriz 'm' est?? recebendo um valor na linha 0", new Integer(0), ((NoInteiro)((NoReferenciaMatriz)atribuicaoM.getOperandoEsquerdo()).getLinha()).getValor());
        Assert.assertEquals("A matriz 'm' est?? recebendo um valor na coluna 1", new Integer(1), ((NoInteiro)((NoReferenciaMatriz)atribuicaoM.getOperandoEsquerdo()).getColuna()).getValor());        
        Assert.assertEquals("A matriz 'm' est?? recebendo um valor armazenado nela mesma", "m", ((NoReferenciaMatriz)atribuicaoM.getOperandoDireito()).getNome());
        Assert.assertEquals("A matriz 'm' recebe o valor armazenado nela mesma na linha 0", new Integer(0), ((NoInteiro)((NoReferenciaMatriz)atribuicaoM.getOperandoDireito()).getLinha()).getValor());
        Assert.assertEquals("A matriz 'm' recebe o valor armazenado nela mesma na coluna 0", new Integer(0), ((NoInteiro)((NoReferenciaMatriz)atribuicaoM.getOperandoDireito()).getColuna()).getValor());
        
        // testando a linha 'm[v[0]][v[1]] = m[m[0][0]][v[0]]'
        NoOperacaoAtribuicao atribuicaoM2 = (NoOperacaoAtribuicao)funcaoInicio.getBlocos().get(4);
        Assert.assertEquals("A matriz 'm' est?? recebendo um valor", "m", ((NoReferenciaMatriz)atribuicaoM2.getOperandoEsquerdo()).getNome());
        Assert.assertTrue("'m' recebe um valor no ??ndice de linha que ?? uma refer??ncia para um vetor", ((NoReferenciaMatriz)atribuicaoM2.getOperandoEsquerdo()).getLinha() instanceof NoReferenciaVetor);
        Assert.assertTrue("'m' recebe um valor no ??ndice de coluna que tamb??m ?? uma refer??ncia para um vetor", ((NoReferenciaMatriz)atribuicaoM2.getOperandoEsquerdo()).getColuna() instanceof NoReferenciaVetor);
        Assert.assertTrue("'m' recebe um valor armazenado em uma matriz", atribuicaoM2.getOperandoDireito() instanceof NoReferenciaMatriz);
        Assert.assertTrue("'m' recebe um valor de uma matriz na linha que tamb??m ?? uma matriz", ((NoReferenciaMatriz)atribuicaoM2.getOperandoDireito()).getLinha() instanceof  NoReferenciaMatriz);
        Assert.assertTrue("'m' recebe um valor de uma matriz na coluna que ?? um vetor", ((NoReferenciaMatriz)atribuicaoM2.getOperandoDireito()).getColuna()instanceof  NoReferenciaVetor);
        
    }

    private void assertNoDeclaracaoParametro(NoDeclaracaoParametro parametro, String nomeEsperado, TipoDado tipoEsperado, ModoAcesso modoAcesso, Quantificador quantificador) {
        Assert.assertEquals("O nome do par??metro deveria ser " + nomeEsperado, nomeEsperado, parametro.getNome());
        Assert.assertEquals("O tipo do par??metro " + nomeEsperado + " deveria ser " + tipoEsperado.getNome(), tipoEsperado, parametro.getTipoDado());
        Assert.assertEquals("Problema no par??metro passado (ou n??o) como refer??ncia", modoAcesso, parametro.getModoAcesso());
        Assert.assertEquals("problema com par??metro que ?? array ou matriz", quantificador, parametro.getQuantificador());
    }
    
    private void assertNoChamadaFuncao(NoChamadaFuncao chamadaFuncao, String nomeEsperado, Object[] parametrosEsperados) {
        assertNoChamadaFuncao(chamadaFuncao, nomeEsperado, (String)null, parametrosEsperados); // escopo nulo
    }
    
    private <T> void assertNoChamadaFuncao(NoChamadaFuncao chamadaFuncao, String nomeEsperado, String escopoEsperado, Object[] parametrosEsperados) {
        assertNoChamadaFuncao(chamadaFuncao, nomeEsperado, escopoEsperado);
        Assert.assertEquals("n??mero de par??metros diferente do esperado", parametrosEsperados.length, chamadaFuncao.getParametros().size());
        for (int i = 0; i < parametrosEsperados.length; i++) {
            Object parametroEsperado = parametrosEsperados[i];
            NoExpressao parametroPassado = chamadaFuncao.getParametros().get(i);
            if (parametroPassado instanceof NoExpressaoLiteral) {
                  Assert.assertEquals("valor do par??metro ?? diferente", parametroEsperado, ((NoExpressaoLiteral<T>) parametroPassado).getValor());
            }
            else if (parametroPassado instanceof NoOperacao) {
                Assert.assertEquals("Classe ?? diferente", parametroEsperado.getClass().getName(), parametroPassado.getClass().getName());
            }
        }
    }
  
    private void assertNoChamadaFuncao(NoChamadaFuncao chamadaFuncao, String nomeEsperado, String escopoEsperado) {
        Assert.assertEquals("O nome da fun????o ?? diferente do esperado", nomeEsperado, chamadaFuncao.getNome());
        Assert.assertEquals("O escopo da fun????o ?? diferente do esperado", escopoEsperado, chamadaFuncao.getEscopoBiblioteca());
    }
    
    private void assertNoInclusaoBiblioteca(NoInclusaoBiblioteca biblioteca, String nomeEsperado) {
        Assert.assertEquals("O nome da biblioteca inclu??da deveria ser " + nomeEsperado, nomeEsperado, biblioteca.getNome());
    }
    
    private void assertNoInclusaoBiblioteca(NoInclusaoBiblioteca biblioteca, String nomeEsperado, String apelidoEsperado) {
        assertNoInclusaoBiblioteca(biblioteca, nomeEsperado);
        Assert.assertEquals("O apelido da biblioteca inclu??da deveria ser " + apelidoEsperado, apelidoEsperado, biblioteca.getAlias());
    }
    
    private <T> void assertNoDeclaracaoVariavel(NoDeclaracaoVariavel declaracaoVariavel, String nomeEsperado, TipoDado tipoEsperado, Class<? extends NoExpressao> classeNoOperacao) {
        assertNoDeclaracaoVariavel(declaracaoVariavel, nomeEsperado, tipoEsperado);
        Assert.assertEquals("Problema na inicializa????o", classeNoOperacao.getName(), declaracaoVariavel.getInicializacao().getClass().getName());
    }
    
    private <T> void assertNoDeclaracaoVariavel(NoDeclaracaoVariavel declaracaoVariavel, String nomeEsperado, TipoDado tipoEsperado, T valorInicial) {
        assertNoDeclaracaoVariavel(declaracaoVariavel, nomeEsperado, tipoEsperado);
        Assert.assertEquals("A vari??vel " + nomeEsperado + " est?? inicializada com o valor " + valorInicial, valorInicial, ((NoExpressaoLiteral<T>)declaracaoVariavel.getInicializacao()).getValor());            
    }
    
    private void assertNoDeclaracaoVariavel(NoDeclaracaoVariavel declaracaoVariavel, String nomeEsperado, TipoDado tipoEsperado) {
        Assert.assertEquals("O nome da vari??vel deveria ser " + nomeEsperado, nomeEsperado, declaracaoVariavel.getNome());
        Assert.assertEquals("O tipo da vari??vel " + nomeEsperado + " deveria ser " + tipoEsperado.getNome(), tipoEsperado, declaracaoVariavel.getTipoDado());
        //Assert.assertFalse("A vari??vel " + nomeEsperado + " est?? inicializada", declaracaoVariavel.temInicializacao());
    }
    
    private <T> void assertNoDeclaracaoMatriz(NoDeclaracaoMatriz noMatriz, String nomeEsperado, T[][] valoresEsperados) throws ExcecaoVisitaASA {
        assertNoDeclaracaoMatriz(noMatriz, nomeEsperado, valoresEsperados.length, valoresEsperados[0].length);
        
        NoMatriz matriz = (NoMatriz)noMatriz.getInicializacao();
        for (int i = 0; i < valoresEsperados.length; i++) {
            for (int j = 0; j < valoresEsperados[0].length; j++) {
                T valor = ((NoExpressaoLiteral<T>)(matriz.getValores().get(i).get(j))).getValor();
                Assert.assertEquals("valores s??o diferentes", valoresEsperados[i][j], valor);
            }
        }
    }
    
    private void assertNoDeclaracaoMatriz(NoDeclaracaoMatriz noMatriz, String nomeEsperado, NoDeclaracaoVariavel linhas, NoDeclaracaoVariavel colunas) throws ExcecaoVisitaASA {
        
        Assert.assertEquals("O nome da matriz deveria ser " + nomeEsperado, nomeEsperado, noMatriz.getNome());
        
        Assert.assertTrue("a vari??vel usada como n??mero de linhas n??o ?? constante", linhas.constante());
        Assert.assertTrue("a vari??vel usada como n??mero de colunas n??o ?? constante", colunas.constante());
        
        Assert.assertEquals("erro na vari??vel usada como n??mero de linhas", linhas.getNome(), ((NoReferenciaVariavel)noMatriz.getNumeroLinhas()).getNome());
        Assert.assertEquals("erro na vari??vel usada como n??mero de colunas", colunas.getNome(), ((NoReferenciaVariavel)noMatriz.getNumeroColunas()).getNome());
       
    }
    
    private void assertNoDeclaracaoMatriz(NoDeclaracaoMatriz noMatriz, String nomeEsperado, int linhas, int colunas) throws ExcecaoVisitaASA {
        
        Assert.assertEquals("O nome da matriz deveria ser " + nomeEsperado, nomeEsperado, noMatriz.getNome());
        
        // assumindo que se este m??todo foi usado a matriz tem suas dimens??es definidas nos colchetes
        NoInteiro noLinhas = (NoInteiro)noMatriz.getNumeroLinhas();
        NoInteiro noColunas = (NoInteiro)noMatriz.getNumeroColunas();
        if (noLinhas != null) {
            Assert.assertEquals("O n??mero de linhas da matriz deveria ser um NoInteiro com valor " + linhas, new Integer(linhas), noLinhas.getValor());
        }
        
        if (noColunas != null) {
            Assert.assertEquals("O n??mero de colunas da matriz deveria ser um NoInteiro com valor " + colunas, new Integer(colunas), noColunas.getValor());
        }
    }
    
    private void assertNoDeclaracaoVetor(NoDeclaracaoVetor noVetor, String nomeEsperado, int tamanhoVetor)
    {
        Assert.assertEquals("O nome do vetor deveria ser " + nomeEsperado, nomeEsperado, noVetor.getNome());
        
        Assert.assertEquals("tamanho do vetor ?? diferente", new Integer(tamanhoVetor), ((NoInteiro)noVetor.getTamanho()).getValor());
    }
    
    private <T> void assertNoDeclaracaoVetor(NoDeclaracaoVetor noVetor, String nomeEsperado, NoDeclaracaoVariavel tamanhoVetor) {
        Assert.assertEquals("O nome do vetor deveria ser " + nomeEsperado, nomeEsperado, noVetor.getNome());
        
        Assert.assertTrue("vari??vel usada como tamanho n??o ?? uma constante", tamanhoVetor.constante());
        
        Assert.assertEquals("erro na vari??vel usada como tamanho", tamanhoVetor.getNome(), ((NoReferenciaVariavel)noVetor.getTamanho()).getNome());
    }
    
    private <T> void assertNoDeclaracaoVetor(NoDeclaracaoVetor noVetor, String nomeEsperado, T[] valoresEsperados)
    {
        Assert.assertEquals("O nome do vetor deveria ser " + nomeEsperado, nomeEsperado, noVetor.getNome());
        
        if (valoresEsperados.length == 0) {
            return;
        }
        
        NoVetor vetor = (NoVetor)noVetor.getInicializacao();
        
        List valoresVetor = extraiValores(vetor);
        
        Assert.assertEquals("A lista de valores do vetor deveria ter o mesmo tamanho da lista de valores esperados", valoresVetor.size(), valoresEsperados.length);
      
        Assert.assertEquals("Os valores do vetor e valores esperados s??o diferentes", Arrays.asList(valoresEsperados), valoresVetor);
        
    }
    
    private <T> List<T> extraiValores(NoVetor vetor) {
        List<T> valores = new ArrayList<>();
        for (Object v : vetor.getValores()) {
            T valor = ((NoExpressaoLiteral<T>)v).getValor();
            valores.add(valor);
        }
        return valores;
    }
    
    private NoDeclaracaoFuncao getNoDeclaracaoFuncao(String nomeFuncao, ASA asa) throws ExcecaoVisitaASA {
        BuscadorFuncao buscador = new BuscadorFuncao(nomeFuncao);
        asa.aceitar(buscador);
        return buscador.getDeclaracaoFuncao();
    }
    
    @Test
    public void testLoopPara() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {                          "
                + "     funcao inicio() {                                       "
                + "         para (inteiro i=0; i < 10; i++) {                   "
                + "             escreva(i)                                      "
                + "         }                                                   "
                + "         inteiro x                                           "
                 + "        para (x=0; x < x+1; x++)                            "
                + "             escreva(x)                                      "
                + "                                                             "
                + "         inteiro j=0                                     "
                + "         para(j; j <= 10; j++) {                         "
                + "            escreva(j)                                  "
                + "         }                                               "
                + "     }                                                       "
                + "}                                                            ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();

        NoDeclaracaoFuncao funcaoInicio = getNoDeclaracaoFuncao("inicio", asa);
        List<NoBloco> blocos = funcaoInicio.getBlocos();
        
        Assert.assertEquals("Eram esperados 5 blocos dentro da fun????o in??cio", 5, blocos.size());
        
        NoPara primeiroNoPara = (NoPara)blocos.get(0);
        NoPara segundoNoPara = (NoPara)blocos.get(2);
        
        Assert.assertTrue("O primeiro loop deveria ter um comando aninhado", primeiroNoPara.getBlocos().size() == 1);
        Assert.assertTrue("O segundo loop deveria ter um comando aninhado", segundoNoPara.getBlocos().size() == 1);
        
        Assert.assertEquals("O primeiro loop Para deveria ter um bloco dentro dele (o comando 'escreva(i)')", 1, primeiroNoPara.getBlocos().size());
        Assert.assertEquals("O segundo loop Para deveria ter um bloco dentro dele (o comando 'escreva(x)')", 1, segundoNoPara.getBlocos().size());
        
        Assert.assertTrue("O primeiro loop Para deveria ter uma inicializa????o", !primeiroNoPara.getInicializacoes().isEmpty());
        Assert.assertTrue("O segundo loop Para deveria ter uma inicializa????o", !segundoNoPara.getInicializacoes().isEmpty());
        
        Assert.assertTrue("A inicializa????o do primeiro loop para deveria ser uma declara????o de vari??vel", primeiroNoPara.getInicializacoes().get(0) instanceof NoDeclaracaoVariavel);
        
        NoDeclaracaoVariavel noDeclaracao = (NoDeclaracaoVariavel)primeiroNoPara.getInicializacoes().get(0);
        Assert.assertTrue("A nome da vari??vel de inicializa????o do primeiro loop deveria ser 'i'", "i".equals(noDeclaracao.getNome()));
        Assert.assertTrue("A tipo da vari??vel de inicializa????o do primeiro loop deveria ser 'inteiro'", "inteiro".equals(noDeclaracao.getTipoDado().getNome()));
        Assert.assertTrue("A vari??vel de inicializa????o do primeiro loop deveria estar inicializada", noDeclaracao.temInicializacao());
        Assert.assertTrue("A vari??vel de inicializa????o do primeiro loop deveria estar inicializada com ZERO", ((NoInteiro)noDeclaracao.getInicializacao()).getValor() == 0);
        
        NoOperacaoAtribuicao noAtribuicao = (NoOperacaoAtribuicao)segundoNoPara.getInicializacoes().get(0);
        NoReferenciaVariavel noRefVariavel = (NoReferenciaVariavel)noAtribuicao.getOperandoEsquerdo();
        Assert.assertTrue("A nome da vari??vel de inicializa????o do segundo loop deveria ser 'x'", "x".equals(noRefVariavel.getNome()));
        Assert.assertTrue("A vari??vel de inicializa????o do segundo loop deveria estar inicializada", noAtribuicao.getOperandoDireito() != null);
        Assert.assertTrue("A vari??vel de inicializa????o do segundo loop deveria estar inicializada com ZERO", ((NoInteiro)noAtribuicao.getOperandoDireito()).getValor() == 0);
        
        
        Assert.assertTrue("A condi????o do primeiro loop deveria ser uma opera????o relacional do tipo < ", primeiroNoPara.getCondicao() instanceof NoOperacaoLogicaMenor);
        Assert.assertTrue("A condi????o do segundo loop deveria ser uma opera????o relacional do tipo < ", segundoNoPara.getCondicao() instanceof NoOperacaoLogicaMenor);
        
        NoOperacao primeiraCondicao = (NoOperacao)primeiroNoPara.getCondicao();
        Assert.assertTrue("A condi????o deveria usar a vari??vel 'i'", "i".equals(((NoReferenciaVariavel)primeiraCondicao.getOperandoEsquerdo()).getNome())); 
        Assert.assertTrue("A condi????o compara 'i' com o valor 10", (((NoInteiro)primeiraCondicao.getOperandoDireito()).getValor()) == 10); 
        
        NoOperacao segundaCondicao = (NoOperacao)segundoNoPara.getCondicao();
        Assert.assertTrue("A condi????o deveria usar a vari??vel 'x'", "x".equals(((NoReferenciaVariavel)segundaCondicao.getOperandoEsquerdo()).getNome())); 
        Assert.assertTrue("A condi????o compara 'x' com uma express??o de soma", segundaCondicao.getOperandoDireito() instanceof NoOperacaoSoma); 

        NoOperacaoAtribuicao primemiroIncremento = (NoOperacaoAtribuicao)primeiroNoPara.getIncremento();
        Assert.assertTrue("O primeiro loop deveria ter uma express??o de incremento", primemiroIncremento != null);
        Assert.assertTrue("O incremento do primeiro loop deveria usar a vari??vel i", "i".equals(((NoReferenciaVariavel)primemiroIncremento.getOperandoEsquerdo()).getNome()));
        
        NoOperacaoAtribuicao segundoIncremento = (NoOperacaoAtribuicao)segundoNoPara.getIncremento();
        Assert.assertTrue("O segundo loop deveria ter uma express??o de incremento", segundoIncremento != null);
        Assert.assertTrue("O incrementod o segundo loop deveria usar a vari??vel x", "x".equals(((NoReferenciaVariavel)segundoIncremento.getOperandoEsquerdo()).getNome()));
    
        NoPara ultimoPara =(NoPara) funcaoInicio.getBlocos().get(4);
        Assert.assertEquals("erro no nome da vari??vel usada na inicializa????o do loop", "j", ((NoReferenciaVariavel)ultimoPara.getInicializacoes().get(0)).getNome());
    }
    
    @Test
    public void testProgramaVazio() throws IOException, RecognitionException, ExcecaoVisitaASA {
        PortugolParser parser = novoParser("programa {                          "
                + "     funcao inicio() {                                       "
                + "                                                             "
                + "     }                                                       "
                + "}                                                            ");

        GeradorASA gerador = new GeradorASA(parser);
        ASAPrograma asa = (ASAPrograma) gerador.geraASA();

        Assert.assertNotNull(asa);

        // procura a fun????o in??cio
        BuscadorFuncao buscador = new BuscadorFuncao("inicio");
        asa.aceitar(buscador);
        
        Assert.assertTrue("A fun????o inicio n??o foi encontrada", buscador.encontrou());
    }

    private class BuscadorFuncao extends VisitanteNulo {

        private final String nomeFuncao;
        private NoDeclaracaoFuncao declaracaoFuncao;

        public BuscadorFuncao(String nomeFuncao) {
            this.nomeFuncao = nomeFuncao;
        }

        public NoDeclaracaoFuncao getDeclaracaoFuncao() {
            return declaracaoFuncao;
        }

        public boolean encontrou()
        {
            return declaracaoFuncao != null;
        }

        @Override
        public Object visitar(NoDeclaracaoFuncao declaracaoFuncao) throws ExcecaoVisitaASA {
            if (nomeFuncao.equals(declaracaoFuncao.getNome())) {
                this.declaracaoFuncao = declaracaoFuncao;
            }
            
            return null;
        }
    }

    private PortugolParser novoParser(String testString) throws IOException {

        return TestUtils.novoParser(testString);
    }

}
