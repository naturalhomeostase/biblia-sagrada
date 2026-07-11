# Como instalar o app sem usar o Android Studio

Este guia usa o **GitHub** (gratuito) para compilar o app na nuvem. Você só precisa de um
navegador — nada de instalar Android Studio, emulador, ou lidar com caminhos/cache no Windows.

## Passo 1 — Criar uma conta no GitHub (se ainda não tiver)

Acesse **https://github.com/signup** e crie uma conta gratuita.

## Passo 2 — Criar um repositório novo

1. No canto superior direito do GitHub, clique no **+** → **New repository**
2. Dê um nome, por exemplo `biblia-sagrada`
3. Deixe como **Public** (ou Private, tanto faz)
4. **Não** marque nenhuma opção de "Add README" — deixe tudo desmarcado
5. Clique em **Create repository**

## Passo 3 — Enviar os arquivos do projeto

Na página do repositório recém-criado, vai aparecer um link **"uploading an existing file"**
(ou **"Add file" → "Upload files"** no menu). Clique nele.

Agora, no seu computador:
1. **Extraia o zip primeiro** (`BibliaSagrada.zip`) numa pasta — não dá para subir o `.zip`
   direto, o GitHub precisa dos arquivos soltos (pastas, `build.gradle.kts`, etc.), senão a
   compilação automática não encontra nada
2. Abra a pasta extraída no Explorador de Arquivos do Windows
3. Selecione **todos** os arquivos e pastas dentro dela (Ctrl+A)
4. Arraste tudo para a área de upload do GitHub no navegador
5. Espere o upload terminar (pode demorar um pouco, o banco de dados tem uns 8MB)
6. Role até o final da página e clique em **Commit changes**

> Dica: se o navegador travar tentando arrastar muitas pastas de uma vez, uma alternativa mais
> confiável é instalar o **GitHub Desktop** (https://desktop.github.com — também gratuito e
> com interface gráfica simples, sem linha de comando) e usar "Add local repository" apontando
> para a pasta do projeto, depois "Publish repository".

## Passo 4 — Deixar o GitHub compilar o APK

1. No repositório, clique na aba **Actions** (no menu superior)
2. Você vai ver um workflow chamado **"Build APK"** rodando (ou clique nele e depois em
   **"Run workflow"** se não tiver iniciado sozinho)
3. Espere terminar — leva de 3 a 8 minutos normalmente. Uma bolinha verde ✅ indica sucesso;
   um X vermelho ❌ indica erro (nesse caso, clique para ver o log e me envie a mensagem)

## Passo 5 — Baixar o APK pronto

1. Ainda na aba **Actions**, clique na execução que terminou com sucesso (✅)
2. Role até a seção **Artifacts** no final da página
3. Clique em **biblia-sagrada-apk** para baixar um arquivo `.zip` contendo o `app-debug.apk`
4. Extraia esse zip — dentro está o `app-debug.apk`

## Passo 6 — Instalar no celular

1. Transfira o arquivo `app-debug.apk` para o seu celular Android (por cabo USB, e-mail para
   você mesmo, Google Drive, WhatsApp — qualquer forma de transferência de arquivo funciona)
2. No celular, abra o arquivo `.apk` (pelo gerenciador de arquivos, por exemplo)
3. O Android vai avisar que "instalar apps de fontes desconhecidas" precisa ser permitido —
   siga a instrução na tela para permitir isso **só para o app que está abrindo o arquivo**
   (geralmente o gerenciador de arquivos ou o navegador)
4. Toque em **Instalar**

Pronto — o app "Bíblia Sagrada" vai aparecer normalmente na tela inicial do celular, como
qualquer outro app, sem precisar de Play Store nem de computador nenhum depois disso.

## Compartilhar com outras pessoas (link público, sem precisar de conta no GitHub)

O workflow já está configurado para, a cada envio de arquivos (`Commit changes`), publicar
automaticamente uma **Release** pública no repositório com o APK anexado — essa é a forma
correta de compartilhar, porque gera um link permanente que qualquer pessoa acessa e baixa
sem precisar ter conta no GitHub (diferente do link de "Artifacts" do Passo 5, que exige
login e expira em 90 dias).

Para pegar esse link:
1. No repositório, clique em **Releases** (aparece na barra lateral direita da página principal,
   ou em `https://github.com/SEU_USUARIO/SEU_REPOSITORIO/releases`)
2. Clique na release mais recente ("Bíblia Sagrada — última versão")
3. Clique com o botão direito no arquivo `app-debug.apk` listado em "Assets" e escolha
   **"Copiar link"** (ou apenas copie a URL da página — ambos funcionam)
4. Envie esse link para quem quiser — a pessoa abre pelo celular, baixa o `.apk` e instala
   do mesmo jeito descrito no Passo 6

> Importante: como esse é um APK de "debug" (não passou pela Google Play), o Android pode
> mostrar um aviso genérico de "app não reconhecido" (Play Protect) ao instalar — é esperado
> para qualquer app instalado fora da Play Store, não indica problema real. A pessoa pode
> tocar em "Instalar mesmo assim".

## E se eu atualizar o projeto depois?

Sempre que você (ou eu) enviar arquivos novos para esse mesmo repositório no GitHub, a aba
**Actions** compila uma nova versão automaticamente — e a Release pública ("ultima-versao")
é atualizada sozinha com o APK mais recente, mantendo o mesmo link de compartilhamento.
Você só repete os passos 4, 5 e 6 — não precisa recriar o repositório nem reenviar o link
para quem já tem.
