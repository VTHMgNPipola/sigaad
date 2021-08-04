# sigaad

![Logotipo sigaad](./sigaad.png)

O sigaad é um daemon que é executado diretamente no seu computador (ou em um servidor seu, se desejar), e pode ser
utilizado por outras aplicações para se comunicar com o SIGAA (atualmente somente o SIGAA do IFSC).

As aplicações podem se comunicar com o sigaad diretamente, utilizando sockets, ou através de uma biblioteca auxiliadora,
como o libsigaa para a linguagem de programação Java.

## Aviso

O sigaad e qualquer aplicação proveniente dele **não** deve ser usado em computadores públicos! O uso é permitido **
somente** na própria máquina pessoal do usuário, que somente ele pode acessar. O SIGAA implementa funcionalidades para
garantir a segurança e privacidade do usuário em computadores públicos, mas o sigaad tem mecanismos para tornar a
experiência melhor que removem essa segurança.

O sigaad faz isso ao fornecer a capacidade de salvar a sessão dos usuários em um arquivo (para que possa ser recuperada
entre execuções do sigaad) e armazenar o nome de usuário e senha do usuário no seu arquivo de configuração (para que o
sigaad possa logar automaticamente no SIGAA quando a sessão expirar).

Além disso, *nunca*, em hipótese alguma, compartilhe os arquivos `sessao.obj`, dentro da pasta de dados do sigaad, e
`sigaad.properties`, dentro da pasta de configurações do sigaad. Estes arquivos podem conter informações válidas de
login do usuário.
