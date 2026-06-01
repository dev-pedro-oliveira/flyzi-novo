// ============================================================
// CONFIGURAÇÃO
// ============================================================

const API_URL = 'https://flyzi-novo.onrender.com/api';

// ============================================================
// DOM ELEMENTS
// ============================================================

const idaVoltaCheckbox = document.getElementById('ida-volta');
const origemSelect = document.getElementById('origem');
const destinoSelect = document.getElementById('destino');
const passageirosInput = document.getElementById('passageiros');
const dataIdaInput = document.getElementById('data-ida');
const dataVoltaInput = document.getElementById('data-volta');
const voltaField = document.getElementById('volta-field');
const calendarIda = document.getElementById('calendar-ida');
const calendarVolta = document.getElementById('calendar-volta');
const searchForm = document.getElementById('search-form');

// ============================================================
// STATE
// ============================================================

let state = {
    idaVolta: true,
    origem: '',
    destino: '',
    passageiros: 1,
    dataIda: '',
    dataVolta: '',
    currentMonth: new Date().getMonth(),
    currentYear: new Date().getFullYear(),
    activeCalendar: null,
    aeroportos: []
};

// ============================================================
// INICIALIZAÇÃO
// ============================================================

document.addEventListener('DOMContentLoaded', async () => {
    console.log('🚀 Iniciando Flyzi Landing...');
    await carregarAeroportos();
    setupEventListeners();
    mostrarVoltaField();
});

// ============================================================
// CARREGAR AEROPORTOS DA API
// ============================================================

async function carregarAeroportos() {
    try {
        console.log('📍 Buscando aeroportos da API...');
        const response = await fetch(API_URL + '/aeroportos');
        
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        
        const aeroportos = await response.json();
        state.aeroportos = aeroportos;
        
        console.log('✅ ' + aeroportos.length + ' aeroportos carregados');
        
        // Preencher os selects
        preencherSelects(aeroportos);
        
    } catch (error) {
        console.error('❌ Erro ao carregar aeroportos:', error);
        origemSelect.innerHTML = '<option value="">Erro ao carregar aeroportos</option>';
    }
}

function preencherSelects(aeroportos) {
    // Preencher Origem
    origemSelect.innerHTML = '<option value="">Selecione um aeroporto</option>';
    aeroportos.forEach(aero => {
        const option = document.createElement('option');
        option.value = aero.iata;
        option.textContent = `${aero.iata} - ${aero.nome}`;
        origemSelect.appendChild(option);
    });

    // Preencher Destino
    destinoSelect.innerHTML = '<option value="">Qualquer lugar</option>';
    aeroportos.forEach(aero => {
        const option = document.createElement('option');
        option.value = aero.iata;
        option.textContent = `${aero.iata} - ${aero.nome}`;
        destinoSelect.appendChild(option);
    });

    console.log('✅ Selects preenchidos com aeroportos');
}

// ============================================================
// EVENT LISTENERS
// ============================================================

function setupEventListeners() {
    idaVoltaCheckbox.addEventListener('change', () => {
        state.idaVolta = idaVoltaCheckbox.checked;
        mostrarVoltaField();
        if (!state.idaVolta) {
            dataVoltaInput.value = '';
            state.dataVolta = '';
        }
    });

    origemSelect.addEventListener('change', (e) => {
        state.origem = e.target.value;
    });

    destinoSelect.addEventListener('change', (e) => {
        state.destino = e.target.value;
    });

    passageirosInput.addEventListener('change', (e) => {
        state.passageiros = parseInt(e.target.value);
    });

    dataIdaInput.addEventListener('click', () => {
        abrirCalendar('ida');
    });

    dataVoltaInput.addEventListener('click', () => {
        abrirCalendar('volta');
    });

    document.addEventListener('click', (e) => {
        if (!e.target.closest('.field')) {
            fecharCalendars();
        }
    });

    searchForm.addEventListener('submit', (e) => {
        e.preventDefault();
        buscar();
    });
}

function mostrarVoltaField() {
    if (state.idaVolta) {
        voltaField.style.display = 'flex';
    } else {
        voltaField.style.display = 'none';
    }
}

// ============================================================
// CALENDAR FUNCTIONS
// ============================================================

function abrirCalendar(tipo) {
    fecharCalendars();
    state.activeCalendar = tipo;
    const calendarDiv = tipo === 'ida' ? calendarIda : calendarVolta;
    gerarCalendar(calendarDiv, tipo);
    calendarDiv.classList.add('active');
}

function fecharCalendars() {
    calendarIda.classList.remove('active');
    calendarVolta.classList.remove('active');
    state.activeCalendar = null;
}

function gerarCalendar(container, tipo) {
    container.innerHTML = '';
    
    const hoje = new Date();
    const mês = state.currentMonth;
    const ano = state.currentYear;
    
    // Header
    const header = document.createElement('div');
    header.className = 'calendar-header';
    header.innerHTML = `
        <button type="button">&larr;</button>
        <h3>${getNomeMês(mês)} ${ano}</h3>
        <button type="button">&rarr;</button>
    `;
    
    header.querySelector('button:first-child').addEventListener('click', (e) => {
        e.preventDefault();
        mesAnterior();
    });
    
    header.querySelector('button:last-child').addEventListener('click', (e) => {
        e.preventDefault();
        proximoMês();
    });
    
    container.appendChild(header);
    
    // Dias da semana
    const weekdaysDiv = document.createElement('div');
    weekdaysDiv.className = 'calendar-weekdays';
    ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'].forEach(dia => {
        const span = document.createElement('div');
        span.className = 'calendar-weekday';
        span.textContent = dia;
        weekdaysDiv.appendChild(span);
    });
    container.appendChild(weekdaysDiv);
    
    // Dias
    const daysDiv = document.createElement('div');
    daysDiv.className = 'calendar-days';
    
    const primeiroDia = new Date(ano, mês, 1).getDay();
    const diasNoMês = new Date(ano, mês + 1, 0).getDate();
    const diasMêsAnterior = new Date(ano, mês, 0).getDate();
    
    // Dias do mês anterior
    for (let i = primeiroDia - 1; i >= 0; i--) {
        const btn = document.createElement('button');
        btn.className = 'calendar-day other-month';
        btn.textContent = diasMêsAnterior - i;
        btn.disabled = true;
        daysDiv.appendChild(btn);
    }
    
    // Dias do mês atual
    for (let dia = 1; dia <= diasNoMês; dia++) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'calendar-day';
        btn.textContent = dia;
        
        const data = new Date(ano, mês, dia);
        
        // Marcar hoje
        if (
            data.getDate() === hoje.getDate() &&
            data.getMonth() === hoje.getMonth() &&
            data.getFullYear() === hoje.getFullYear()
        ) {
            btn.classList.add('today');
        }
        
        // Desabilitar datas passadas
        if (data < new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate())) {
            btn.disabled = true;
        }
        
        // Marcar selecionada
        if (tipo === 'ida' && state.dataIda === formatarData(data)) {
            btn.classList.add('selected');
        } else if (tipo === 'volta' && state.dataVolta === formatarData(data)) {
            btn.classList.add('selected');
        }
        
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            selecionarData(tipo, data);
        });
        
        daysDiv.appendChild(btn);
    }
    
    // Dias do próximo mês
    const diasRestantes = (35 - primeiroDia - diasNoMês);
    for (let dia = 1; dia <= diasRestantes; dia++) {
        const btn = document.createElement('button');
        btn.className = 'calendar-day other-month';
        btn.textContent = dia;
        btn.disabled = true;
        daysDiv.appendChild(btn);
    }
    
    container.appendChild(daysDiv);
}

function mesAnterior() {
    state.currentMonth--;
    if (state.currentMonth < 0) {
        state.currentMonth = 11;
        state.currentYear--;
    }
    const tipo = state.activeCalendar;
    const calendarDiv = tipo === 'ida' ? calendarIda : calendarVolta;
    gerarCalendar(calendarDiv, tipo);
}

function proximoMês() {
    state.currentMonth++;
    if (state.currentMonth > 11) {
        state.currentMonth = 0;
        state.currentYear++;
    }
    const tipo = state.activeCalendar;
    const calendarDiv = tipo === 'ida' ? calendarIda : calendarVolta;
    gerarCalendar(calendarDiv, tipo);
}

function selecionarData(tipo, data) {
    const dataFormatada = formatarData(data);
    
    if (tipo === 'ida') {
        state.dataIda = dataFormatada;
        dataIdaInput.value = dataFormatada;
        if (state.dataVolta && state.dataVolta < dataFormatada) {
            state.dataVolta = '';
            dataVoltaInput.value = '';
        }
    } else {
        state.dataVolta = dataFormatada;
        dataVoltaInput.value = dataFormatada;
    }
    
    fecharCalendars();
}

function formatarData(data) {
    const dia = String(data.getDate()).padStart(2, '0');
    const mês = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    return `${dia}/${mês}/${ano}`;
}

function getNomeMês(mês) {
    const meses = [
        'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
        'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
    ];
    return meses[mês];
}

// ============================================================
// VALIDAÇÃO E BUSCA
// ============================================================

function buscar() {
    if (!state.origem) {
        alert('Selecione uma cidade de origem');
        return;
    }
    
    const params = new URLSearchParams({
        origem: state.origem,
        destino: state.destino || '',
        dataIda: state.dataIda || '',
        dataVolta: state.dataVolta || '',
        passageiros: state.passageiros,
        idaVolta: state.idaVolta
    });
    
    window.location.href = `resultados.html?${params.toString()}`;
}