package campominado.modelo;

import java.util.ArrayList;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class Tabuleiro implements CampoObservador {
	
	private final int quantidadedelinhas;
	private final int quantidadedecolunas;
	private final int quantidadedeminas;
	
	private final List<Campo> campos = new ArrayList<>();
	private final List<Consumer<ResultadoEvento>> observadores = new ArrayList<>();

	public Tabuleiro(int quantidadedelinhas, int quantidadedecolunas, int quantidadedeminas) {
		this.quantidadedelinhas = quantidadedelinhas;
		this.quantidadedecolunas = quantidadedecolunas;
		this.quantidadedeminas = quantidadedeminas;
		
		gerarCampos();
		associarVizinhos();
		sortearMinas();
	}
	
	public void paraCadaCampo(Consumer<Campo> funcao) {
		campos.forEach(funcao);
		
	}
	
	public void registrarObservador(Consumer<ResultadoEvento> observador ) {
		observadores.add(observador);
	}
	
	private void notificarObservadores(boolean resultado) {
		observadores.stream().forEach(o -> o.accept(new ResultadoEvento (resultado)));
	}
	
	public void abrir(int linha, int coluna) {
			campos.parallelStream().filter(c -> c.getLinha() == linha && c.getColuna() == coluna).findFirst().ifPresent(c -> c.abrir());
	}
	
	public void alternarMarcacao(int linha, int coluna) {
		campos.parallelStream().filter(c -> c.getLinha() == linha && c.getColuna() == coluna).findFirst().ifPresent(c -> c.alternarMarcacao());
	}

	private void gerarCampos() {
		for (int linha = 0; linha < quantidadedelinhas; linha++) {
			for (int coluna = 0; coluna < quantidadedecolunas; coluna++) {
				Campo campo = new Campo(linha, coluna);
				campo.registrarObservador(this);
				campos.add(campo);
			}
		}
		
	}
	
    private void associarVizinhos() {
    	for(Campo c1: campos) {
    		for(Campo c2: campos) {
    			c1.adicionarVizinho(c2);
    		}
    	}

	}

    private void sortearMinas() {
    	long minasArmadas = 0;
    	Predicate<Campo> minado = c -> c.isMinado();
    	
    	do {
    		int aleatorio = (int) (Math.random() * campos.size());
    		campos.get(aleatorio).minar();
    		minasArmadas = campos.stream().filter(minado).count();
    	} while(minasArmadas < quantidadedeminas);
	
    }
    
    public boolean objetivoAlcancado() {
		return campos.stream().allMatch(c -> c.objetivoAlcancado());
	}
    
    public void reiniciar() {
    	campos.stream().forEach(c -> c.reinicar());
    	sortearMinas();
    }    
    
    public int getQuantidadedelinhas() {
		return quantidadedelinhas;
	}

	public int getQuantidadedecolunas() {
		return quantidadedecolunas;
	}

	@Override
    public void eventoOcorreu(Campo c, CampoEvento evento) {
        if(evento == CampoEvento.EXPLODIR) {
        	mostrarMinas();
        	notificarObservadores(false);
        } else if(objetivoAlcancado()){
        	notificarObservadores(true);
        }
    }
    
    private void mostrarMinas() {
		campos.stream().filter(c -> c.isMinado()).filter(c -> !c.isMarcado()).forEach(c -> c.setAberto(true));
	}
}		
