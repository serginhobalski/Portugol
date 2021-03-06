package br.univali.portugol.nucleo.analise.semantica.avisos;

import br.univali.portugol.nucleo.asa.ExcecaoVisitaASA;
import br.univali.portugol.nucleo.asa.NoDeclaracaoBase;
import br.univali.portugol.nucleo.asa.NoDeclaracaoFuncao;
import br.univali.portugol.nucleo.asa.NoDeclaracaoMatriz;
import br.univali.portugol.nucleo.asa.NoDeclaracaoParametro;
import br.univali.portugol.nucleo.asa.NoDeclaracaoVariavel;
import br.univali.portugol.nucleo.asa.NoDeclaracaoVetor;
import br.univali.portugol.nucleo.asa.NoParametroFuncao;
import br.univali.portugol.nucleo.asa.VisitanteASABasico;
import br.univali.portugol.nucleo.mensagens.AvisoAnalise;
import br.univali.portugol.nucleo.simbolos.Funcao;
import br.univali.portugol.nucleo.simbolos.Matriz;
import br.univali.portugol.nucleo.simbolos.Simbolo;
import br.univali.portugol.nucleo.simbolos.Variavel;
import br.univali.portugol.nucleo.simbolos.Vetor;

/**
 *
 * @author Luiz Fernando Noschang
 * @author Fillipi Pelz
 */
public final class AvisoSimboloGlobalOcultado extends AvisoAnalise
{
    private Simbolo simboloGlobal;
    private Simbolo simboloLocal;
    private NoDeclaracaoBase declaracao;
    private NoDeclaracaoParametro noDeclaracaoParametro;
    private String codigo = "AvisoSemantico.AvisoSimboloGlobalOcultado";
    
    public AvisoSimboloGlobalOcultado(Simbolo simboloGlobal, Simbolo simboloLocal, NoDeclaracaoBase declaracao)
    {
        super(declaracao.getTrechoCodigoFonteNome());
        
        this.simboloGlobal = simboloGlobal;
        this.simboloLocal = simboloLocal;
        this.declaracao = declaracao;
        
        this.getMensagem();
        super.setCodigo(codigo);
    }

    public AvisoSimboloGlobalOcultado(Simbolo simboloGlobal, Simbolo simboloLocal, NoDeclaracaoParametro noDeclaracaoParametro)
    {
        super(noDeclaracaoParametro.getTrechoCodigoFonteNome());
        
        this.simboloGlobal = simboloGlobal;
        this.simboloLocal = simboloLocal;
        this.noDeclaracaoParametro = noDeclaracaoParametro;
        
        this.getMensagem();
        super.setCodigo(codigo);
    }
    
     /**
     * {@inheritDoc }
     */
    @Override
    protected String construirMensagem()
    {        
        return new AvisoSimboloGlobalOcultado.ConstrutorMensagem().construirMensagem();
    }
    
    private class ConstrutorMensagem extends VisitanteASABasico
    {
        public ConstrutorMensagem()
        {
            
        }        
        
        public String construirMensagem()
        {
            try
            {
                if (declaracao != null)                
                    return (String) declaracao.aceitar(this);
                else if (noDeclaracaoParametro != null)
                    return (String) noDeclaracaoParametro.aceitar(this);
                else
                    return "Um simbolo est?? sendo ocultado";
            }
            catch (ExcecaoVisitaASA e)
            {
                return e.getMessage();
            }
        }  

        private StringBuilder appendGenericMessage(StringBuilder builder){
            if (declaracao != null) {
                builder.append(declaracao.getNome());
            } 
            else if (noDeclaracaoParametro != null)
            {
                builder.append(noDeclaracaoParametro.getNome());
            }
                    
            builder.append("\" est?? ocultando ");
            
            if (simboloGlobal instanceof Variavel){
                builder.append("uma vari??vel ");
            } else if (simboloGlobal instanceof Vetor){
                builder.append("um vetor ");
            } else if (simboloGlobal instanceof Matriz) {
                builder.append("uma matriz ");
            } else if (simboloGlobal instanceof Funcao) {
                builder.append("uma fun????o ");
            }
            builder.append("do escopo global.");
            
            return builder;
        }
        
        @Override
        public Object visitar(NoDeclaracaoMatriz noDeclaracaoMatriz) throws ExcecaoVisitaASA
        {
            StringBuilder builder = new StringBuilder("A matriz \"");            
            return appendGenericMessage(builder).toString();
        }

        @Override
        public Object visitar(NoDeclaracaoVariavel noDeclaracaoVariavel) throws ExcecaoVisitaASA
        {
            StringBuilder builder = new StringBuilder("A vari??vel \"");            
            return appendGenericMessage(builder).toString();
        }

        @Override
        public Object visitar(NoDeclaracaoParametro noDeclaracaoParametro) throws ExcecaoVisitaASA
        {
            StringBuilder builder = new StringBuilder();
            if (simboloLocal instanceof Variavel){
                builder.append("A vari??vel \"");
            } else if (simboloLocal instanceof Vetor) {
                builder.append("O vetor \"");
            } else if (simboloLocal instanceof Matriz) {
                builder.append("A matriz \"");
            }
            return appendGenericMessage(builder).toString();
        }

        @Override
        public Object visitar(NoDeclaracaoVetor noDeclaracaoVetor) throws ExcecaoVisitaASA
        {
            StringBuilder builder = new StringBuilder("O vetor \"");            
            return appendGenericMessage(builder).toString();
        }

        @Override
        public Object visitar(NoDeclaracaoFuncao declaracaoFuncao) throws ExcecaoVisitaASA
        {
            return super.visitar(declaracaoFuncao); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object visitar(NoParametroFuncao noParametroFuncao) throws ExcecaoVisitaASA {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
