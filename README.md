# Grupo 12 — Prática ABB | Gamificação - Pedro Pizzi

CASE:
GAMIFICAÇÃO PARA ENGAJAMENTO DE EDUCAÇÃO CONTINUADA
Uma determinada plataforma vende cursos online e EAD no modelo de assinaturas. O
aluno paga um valor mensal e tem acesso a um conjunto de cursos para assinatura
básica. A cada curso terminado e com média acima de 7,0, o aluno tem direito a
realização de mais 3 cursos. O aluno que escrever mais tópicos no fórum e ajudar outros
participantes com seus comentários, ganha um curso no final do mês. Quando o aluno
conquistar 12 cursos, seu plano de assinatura passa a ser “Premium” e ele passa a
receber voucher para participar de projetos reais, durante os cursos, e receber 3 moedas,
que podem ser convertidas em conhecimento (novos cursos), acumular ou receber por
criptomoeda.



OBJETIVO INDIVIDUAL:
Dado alunos com diferentes quantidades de tópicos e comentários de ajuda
Quando o engajamento de um aluno é calculado
Então deve ser a soma dos tópicos com os comentários de ajuda




TESTES
RED:
O teste espera que um aluno com 7 tópicos e 3 comentários tenha 10 pontos de engajamento. Como a regra ainda não estava implementada corretamente, o teste falhou.

<img width="886" height="391" alt="image" src="https://github.com/user-attachments/assets/78669db2-d24d-4ab6-a201-f93002785b28" />


GREEN:
Depois da falha, implementei a regra mínima necessária para satisfazer o teste. Agora os três cenários estão passando.

<img width="886" height="389" alt="image" src="https://github.com/user-attachments/assets/3c10c14b-7483-4dad-ad55-38deebd71ebd" />




BLUE: 
Depois de fazer o teste passar, fiz a refatoração e organização da solução. A regra permaneceu no domínio, enquanto o acesso aos dados foi separado em Repository e Entity, o caso de uso ficou no Service e a API no Controller. Além disso, validEI a cobertura dos testes com JaCoCo, chegando a 100% para o domínio.

<img width="886" height="169" alt="image" src="https://github.com/user-attachments/assets/8fc20f54-2294-46dd-96b5-a34ece2b02e4" />
<img width="886" height="266" alt="image" src="https://github.com/user-attachments/assets/80f324e0-0f6f-4a51-a517-861531019c8b" />
<img width="886" height="546" alt="image" src="https://github.com/user-attachments/assets/588c0879-852e-4e4b-9b87-f8f036a7a3c8" />



SWAGGER:
Metodos GET e POST funcionando. 
Adicionado valores em cada Variavel.

<img width="886" height="434" alt="image" src="https://github.com/user-attachments/assets/bcd5b726-1bbb-4639-af4f-b4a797ef9a76" />
<img width="886" height="412" alt="image" src="https://github.com/user-attachments/assets/9b858831-9fef-4f43-bb6a-03dbfe5b18dd" />
<img width="886" height="463" alt="image" src="https://github.com/user-attachments/assets/abe67a8b-54c2-49aa-81f5-0fc29018c7dd" />
<img width="886" height="295" alt="image" src="https://github.com/user-attachments/assets/7610d640-0dbc-44e6-8c42-731b1e25fbe8" />
<img width="886" height="479" alt="image" src="https://github.com/user-attachments/assets/5155de0f-588a-46e0-b8c4-34746920dff2" />



BANCO DE DADOS DOCKER:
<img width="1847" height="890" alt="image" src="https://github.com/user-attachments/assets/93abb64d-669e-41b9-948d-75104afb4dd0" />
<img width="497" height="860" alt="image" src="https://github.com/user-attachments/assets/54c8367d-d887-4eb6-ac16-0e5ce5f79d07" />



FRONTEND PARA VALIDAÇÃO DA ETAPA DESENVOLVIDA:
<img width="886" height="700" alt="image" src="https://github.com/user-attachments/assets/3f3c696a-1642-4a6f-923d-e20600770b4f" />

