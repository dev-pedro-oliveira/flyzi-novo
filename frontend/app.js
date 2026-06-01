// ============================================================
// VARIÁVEIS GLOBAIS
// ============================================================

let voos = [];
let voosOriginais = [];
let searchParams = {};
let favoritos = [];
let historico = [];
let vooSelecionado = null;

// ============================================================
// INICIALIZAÇÃO
// ============================================================

document.addEventListener('DOMContentLoaded', async () => {
    carregarFavoritos();
    carregarHistorico();
    extrairParametros();
    adicionarAoHistorico();
    atualizarTituloBusca();
    await carregarVoos();
    renderizarHistorico();
    configurarEventListeners();
});

// ============================================================
// FUNÇÕES PRINCIPAIS
// ============================================================

function extrairParametros() {
    const params = new URLSearchParams(window.location.search);
    searchParams = {
        origem: params.get('origem') || '',
        destino: params.get('destino') || '',
        dataIda: params.get('dataIda') || '',
        dataVolta: params.get('dataVolta') || '',
        passageiros: parseInt(params.get('passageiros') || '1'),
        idaVolta: params.get('idaVolta') === 'true'
    };
    
    console.log('Parâmetros de busca:', searchParams);
}

function atualizarTituloBusca() {
    const tituloPrincipal = document.getElementById('titulo-busca');
    const tituloSidebar = document.getElementById('titulo-busca-sidebar');
    
    if (searchParams.origem) {
        const destino = searchParams.destino || 'Qualquer lugar';
        const texto = `${searchParams.origem} → ${destino}`;
        tituloPrincipal.textContent = texto;
        tituloSidebar.textContent = texto;
    }
}

async function carregarVoos() {
    try {
        // Mostrar skeleton loading
        mostrarSkeletonLoading();
        
        const response = await fetch('http://localhost:8080/api/voos');
        
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        
        const todosVoos = await response.json();
        console.log('Total de voos no banco:', todosVoos.length);
        
        // Filtrar por origem
        if (searchParams.origem) {
            voos = todosVoos.filter(voo => voo.origem === searchParams.origem);
            console.log('Voos da origem selecionada:', voos.length);
        } else {
            voos = todosVoos;
        }
        
        // Se houver destino, filtrar também
        if (searchParams.destino) {
            voos = voos.filter(voo => voo.destino === searchParams.destino);
            console.log('Voos após filtrar destino:', voos.length);
        }
        
        voosOriginais = [...voos];
        
        if (voos.length === 0) {
            console.warn('Nenhum voo encontrado com os critérios!');
            mostrarToast('Nenhum voo encontrado', 'error');
        } else {
            mostrarToast(`${voos.length} voos encontrados`, 'success');
        }
        
        renderizarVoos();
        
    } catch (error) {
        console.error('Erro ao carregar voos:', error);
        document.getElementById('voos-container').innerHTML = 
            `<div class="vazio">❌ Erro ao carregar voos: ${error.message}</div>`;
        mostrarToast('Erro ao carregar voos', 'error');
    }
}

function configurarEventListeners() {
    document.getElementById('btn-aplicar').addEventListener('click', aplicarFiltros);
    document.getElementById('btn-limpar').addEventListener('click', limparFiltros);
    document.getElementById('sort-dropdown').addEventListener('change', ordenarVoos);
    document.getElementById('drawer-overlay').addEventListener('click', fecharDrawer);
    document.getElementById('btn-close-drawer').addEventListener('click', fecharDrawer);
    
    // Fechar drawer com ESC
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            fecharDrawer();
        }
    });
}

// ============================================================
// FILTROS E ORDENAÇÃO
// ============================================================

function aplicarFiltros() {
    const companhiasSelecionadas = Array.from(document.querySelectorAll('.filter-companhia:checked'))
        .map(cb => cb.value);
    
    const apenasDirectos = document.getElementById('filter-diretos').checked;
    
    voos = voosOriginais.filter(voo => {
        if (companhiasSelecionadas.length > 0 && !companhiasSelecionadas.includes(voo.companhia)) {
            return false;
        }
        
        if (apenasDirectos && voo.tipo !== 'Direto') {
            return false;
        }
        
        return true;
    });
    
    console.log('Voos após aplicar filtros:', voos.length);
    renderizarVoos();
    mostrarToast(`${voos.length} voos encontrados`, 'success');
}

function limparFiltros() {
    document.querySelectorAll('.filter-companhia').forEach(cb => cb.checked = false);
    document.getElementById('filter-diretos').checked = false;
    document.getElementById('sort-dropdown').value = 'preco-asc';
    
    voos = [...voosOriginais];
    console.log('Filtros limpos. Voos exibidos:', voos.length);
    renderizarVoos();
    mostrarToast('Filtros limpos', 'success');
}

function ordenarVoos(e) {
    const tipo = e.target.value;
    
    switch (tipo) {
        case 'preco-asc':
            voos.sort((a, b) => a.preco - b.preco);
            break;
        case 'preco-desc':
            voos.sort((a, b) => b.preco - a.preco);
            break;
        case 'duracao-asc':
            voos.sort((a, b) => a.duracaoMinutos - b.duracaoMinutos);
            break;
        case 'duracao-desc':
            voos.sort((a, b) => b.duracaoMinutos - a.duracaoMinutos);
            break;
    }
    
    console.log('Voos ordenados por:', tipo);
    renderizarVoos();
}

// ============================================================
// RENDERIZAÇÃO
// ============================================================

function mostrarSkeletonLoading() {
    const container = document.getElementById('voos-container');
    container.innerHTML = `
        <div class="voo-skeleton"></div>
        <div class="voo-skeleton"></div>
        <div class="voo-skeleton"></div>
        <div class="voo-skeleton"></div>
    `;
}

function renderizarVoos() {
    const container = document.getElementById('voos-container');
    
    if (voos.length === 0) {
        container.innerHTML = '<div class="vazio">✈️ Nenhum voo encontrado. Tente mudar os critérios de busca.</div>';
        return;
    }
    
    container.innerHTML = voos.map(voo => `
        <div class="voo-item" onclick="abrirDrawer(${voo.id})">
            <div class="voo-info-principal">
                <div class="voo-data">${voo.data}</div>
                <div class="voo-horario">${voo.horario.split('-')[0].trim()}</div>
                <div class="voo-tipo">${voo.tipo}</div>
            </div>
            
            <div class="voo-detalhes">
                <div class="detalhe">
                    <div class="detalhe-label">Companhia</div>
                    <div class="detalhe-valor">${voo.companhia}</div>
                </div>
                <div class="detalhe">
                    <div class="detalhe-label">Duração</div>
                    <div class="detalhe-valor voo-duracao">${voo.duracaoTexto}</div>
                </div>
                <div class="detalhe">
                    <div class="detalhe-label">Rota</div>
                    <div class="detalhe-valor">${voo.descricaoRota}</div>
                </div>
            </div>
            
            <div class="voo-preco">
                <div class="preco-valor">${formatarMoeda(voo.preco)}</div>
                <div class="preco-milhas">${voo.milhasNum.toLocaleString('pt-BR')} milhas</div>
                ${voo.teveQueda ? '<div class="voo-queda">📉 Preço caiu</div>' : ''}
                <button class="btn-ofertar" onclick="event.stopPropagation()">Ver Oferta</button>
            </div>
        </div>
    `).join('');
    
    console.log('Renderizados', voos.length, 'voos');
}

// ============================================================
// DRAWER
// ============================================================

function abrirDrawer(vooId) {
    vooSelecionado = voos.find(v => v.id === vooId);
    
    if (!vooSelecionado) return;
    
    const drawerBody = document.getElementById('drawer-body');
    
    drawerBody.innerHTML = `
        <div class="drawer-info">
            <h4>Informações do Voo</h4>
            <div class="drawer-info-row">
                <span class="drawer-info-label">Companhia</span>
                <span class="drawer-info-value">${vooSelecionado.companhia}</span>
            </div>
            <div class="drawer-info-row">
                <span class="drawer-info-label">Data</span>
                <span class="drawer-info-value">${vooSelecionado.data}</span>
            </div>
            <div class="drawer-info-row">
                <span class="drawer-info-label">Horário</span>
                <span class="drawer-info-value">${vooSelecionado.horario}</span>
            </div>
            <div class="drawer-info-row">
                <span class="drawer-info-label">Duração</span>
                <span class="drawer-info-value">${vooSelecionado.duracaoTexto}</span>
            </div>
            <div class="drawer-info-row">
                <span class="drawer-info-label">Tipo</span>
                <span class="drawer-info-value">${vooSelecionado.tipo}</span>
            </div>
        </div>

        <div class="drawer-info">
            <h4>Rota</h4>
            <div class="drawer-info-row">
                <span class="drawer-info-label">Origem</span>
                <span class="drawer-info-value">${vooSelecionado.origem}</span>
            </div>
            <div class="drawer-info-row">
                <span class="drawer-info-label">Destino</span>
                <span class="drawer-info-value">${vooSelecionado.destino}</span>
            </div>
            <div class="drawer-info-row">
                <span class="drawer-info-label">Continente</span>
                <span class="drawer-info-value">${vooSelecionado.continente}</span>
            </div>
        </div>

        <div class="drawer-info">
            <h4>Preço e Milhas</h4>
            <div class="drawer-info-row">
                <span class="drawer-info-label">Preço</span>
                <span class="drawer-info-value" style="color: var(--azul-claro); font-weight: 700;">${formatarMoeda(vooSelecionado.preco)}</span>
            </div>
            <div class="drawer-info-row">
                <span class="drawer-info-label">Milhas</span>
                <span class="drawer-info-value" style="color: var(--amarelo);">${vooSelecionado.milhasNum.toLocaleString('pt-BR')}</span>
            </div>
            ${vooSelecionado.teveQueda ? `
                <div class="drawer-info-row">
                    <span class="drawer-info-label">Status</span>
                    <span class="drawer-info-value" style="color: var(--amarelo);">📉 Preço caiu</span>
                </div>
            ` : ''}
        </div>
    `;
    
    document.getElementById('btn-comprar').onclick = () => {
        const urlCompanhia = {
            'Azul': 'https://www.azul.com.br',
            'Gol': 'https://www.voegol.com.br',
            'Latam': 'https://www.latam.com/pt_br'
        }[vooSelecionado.companhia] || 'https://www.google.com';
        
        window.open(urlCompanhia, '_blank');
    };
    
    document.getElementById('drawer-overlay').classList.add('active');
    document.getElementById('drawer-detalhes').classList.add('active');
    document.body.style.overflow = 'hidden';
}

function fecharDrawer() {
    document.getElementById('drawer-overlay').classList.remove('active');
    document.getElementById('drawer-detalhes').classList.remove('active');
    document.body.style.overflow = 'auto';
    vooSelecionado = null;
}

// ============================================================
// HISTÓRICO
// ============================================================

function carregarHistorico() {
    const stored = localStorage.getItem('flyzi-historico');
    historico = stored ? JSON.parse(stored) : [];
}

function salvarHistorico() {
    localStorage.setItem('flyzi-historico', JSON.stringify(historico.slice(0, 5)));
}

function adicionarAoHistorico() {
    if (!searchParams.origem) return;
    
    const busca = `${searchParams.origem} → ${searchParams.destino || 'Qualquer lugar'}`;
    
    // Remover se já existe
    historico = historico.filter(h => h !== busca);
    
    // Adicionar no início
    historico.unshift(busca);
    
    // Manter apenas últimas 5
    historico = historico.slice(0, 5);
    
    salvarHistorico();
}

function renderizarHistorico() {
    const container = document.getElementById('historico-list');
    const section = document.getElementById('historico-section');
    
    if (historico.length === 0) {
        section.style.display = 'none';
        return;
    }
    
    section.style.display = 'block';
    
    container.innerHTML = historico.map(busca => `
        <div class="historico-item" onclick="irParaBusca('${busca}')">
            ${busca}
        </div>
    `).join('');
}

function irParaBusca(busca) {
    const [origem, destino] = busca.split(' → ').map(s => s.trim());
    const params = new URLSearchParams({
        origem: origem,
        destino: destino === 'Qualquer lugar' ? '' : destino,
        dataIda: '',
        dataVolta: '',
        passageiros: 1,
        idaVolta: true
    });
    
    window.location.href = `resultados.html?${params.toString()}`;
}

// ============================================================
// FAVORITOS
// ============================================================

function carregarFavoritos() {
    const stored = localStorage.getItem('flyzi-favoritos');
    favoritos = stored ? JSON.parse(stored) : [];
}

function salvarFavoritos() {
    localStorage.setItem('flyzi-favoritos', JSON.stringify(favoritos));
}

function adicionarFavorito(vooId) {
    if (!favoritos.includes(vooId)) {
        favoritos.push(vooId);
        salvarFavoritos();
        mostrarToast('Adicionado aos favoritos', 'success');
    }
}

function removerFavorito(vooId) {
    favoritos = favoritos.filter(id => id !== vooId);
    salvarFavoritos();
    mostrarToast('Removido dos favoritos', 'success');
}

// ============================================================
// TOAST
// ============================================================

function mostrarToast(mensagem, tipo = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = mensagem;
    toast.className = `toast ${tipo} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// ============================================================
// UTILITÁRIOS
// ============================================================

function formatarMoeda(valor) {
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}