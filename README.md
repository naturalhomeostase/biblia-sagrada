# Bíblia Sagrada — App Android (Fase 1)

App de leitura da Bíblia para Android, 100% offline, gratuito, sem anúncios e livre para redistribuição.

> **Não quer lidar com Android Studio?** Veja o arquivo
> [`COMO_INSTALAR_SEM_ANDROID_STUDIO.md`](./COMO_INSTALAR_SEM_ANDROID_STUDIO.md) — um jeito
> de gerar o APK na nuvem (via GitHub Actions) e instalar direto no celular, sem precisar
> compilar nada localmente.

## Texto bíblico usado

Este projeto usa a tradução **Bíblia Livre (BLIVRE)**, edição baseada no Textus Receptus (`bliv-tr`),
licenciada sob **Creative Commons Atribuição 3.0 Brasil (CC BY 3.0 BR)**.

> A Bíblia Livre tem uso livre, porém a menção da obra de forma adequada é obrigatória.

Créditos sugeridos pelos próprios tradutores (mantenha isso em uma tela "Sobre" no app):

```
Todas as Escrituras em português citadas são da Bíblia Livre (BLIVRE),
Copyright © Diego Santos, Mario Sérgio, e Marco Teles,
http://sites.google.com/site/biblialivre/
Licença Creative Commons Atribuição 3.0 Brasil
(http://creativecommons.org/licenses/by/3.0/br/)
```

Fonte original: https://github.com/blivre/BibliaLivre

O texto já está processado e embutido em `app/src/main/assets/biblia.db` (SQLite, 66 livros,
31.102 versículos, com índice de busca full-text FTS4 pronto para uso).

## Ícone do app

O ícone (livro preto com detalhes dourados) já está integrado como ícone adaptativo
(`res/mipmap-*/ic_launcher_foreground.png` + `res/values/ic_launcher_background.xml`),
funcionando em qualquer formato de máscara do launcher (círculo, quadrado arredondado, etc.).

Uma versão de 512x512 com fundo sólido (para publicar na Play Store, que exige PNG sem
transparência) está em `store_assets/icone_play_store_512x512.png`.

## Como abrir o projeto

1. Instale o **Android Studio** (versão Koala/2024.1 ou mais recente).
2. Abra a pasta `BibliaSagrada` como projeto existente (`File > Open`).
3. Aguarde o Gradle sincronizar (ele vai baixar automaticamente o Gradle Wrapper e as
   dependências na primeira vez — precisa de internet só nesse passo único).
4. Rode em um emulador ou celular físico (`Run > Run 'app'`).

Requisitos mínimos: Android 8.0 (API 26) ou superior.

## Funcionalidades implementadas (Fase 1 — essenciais)

- ✅ Leitura 100% offline (banco SQLite embutido no APK, nada é baixado)
- ✅ Escolha de livro → capítulo → versículo
- ✅ Busca por palavra (índice FTS4) com fallback para busca aproximada (tolera erros de digitação)
- ✅ Busca por referência direta (ex: "João 3:16", "jo 3.16", "1 cor 13")
- ✅ Filtro de busca por Antigo/Novo Testamento
- ✅ Tamanho de fonte ajustável (slider nas Configurações)
- ✅ Modo claro / escuro / seguir o sistema
- ✅ Favoritos
- ✅ Histórico dos últimos capítulos lidos
- ✅ "Continuar de onde parei" (card na tela inicial)
- ✅ Compartilhar versículo como texto (Intent nativo do Android)
- ✅ Copiar versículo para a área de transferência
- ✅ Marcação (highlight) com 5 cores, com opção de remover
- ✅ Notas pessoais vinculadas a versículos
- ✅ Navegação por gesto (deslizar) entre capítulos — contínua por toda a Bíblia

## Arquitetura

- **Kotlin + Jetpack Compose** (Material 3) — interface 100% declarativa
- **Dois bancos de dados separados**:
  - `biblia.db`: somente leitura, copiado dos assets no primeiro uso — o texto bíblico e a busca
  - `user_data.db` (Room): favoritos, notas, destaques, histórico, progresso de leitura —
    dados pessoais do usuário, nunca misturados com o texto bíblico
- **DataStore Preferences**: tema e tamanho de fonte
- Sem bibliotecas de anúncios, analytics ou rastreamento de qualquer tipo

## Estrutura de pastas

```
app/src/main/java/com/bibliasagrada/app/
├── data/
│   ├── db/            BibleDatabaseHelper (leitura + busca no biblia.db)
│   ├── room/           Entidades e DAOs do user_data.db
│   ├── model/          Book, Verse, SearchResult, ChapterRef
│   └── repository/     BibleRepository (API única) + PreferencesManager
├── ui/
│   ├── theme/           Cores, tipografia, tema claro/escuro
│   ├── navigation/      Rotas e NavHost
│   ├── screens/         Todas as telas do app
│   └── components/      VerseActionSheet (bottom sheet de ações do versículo)
└── MainActivity.kt
```

## Próximos passos sugeridos (Fase 2, quando quiser evoluir)

- Comparação entre traduções (a arquitetura já suporta múltiplos `biblia_<versao>.db`)
- Planos de leitura e leitura cronológica
- Backup local (exportar/importar favoritos, notas e destaques em JSON)
- Widget de tela inicial com o "versículo do dia"
- Referências cruzadas, usando datasets abertos como o do OpenBible.info

## Licença do app

O código-fonte deste projeto pode ser livremente redistribuído. O texto bíblico embutido
segue a licença CC BY 3.0 BR da Bíblia Livre, descrita acima — mantenha os créditos.
