import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_URL, APP_URL } from '../config';

function AdminWiki() {
    const { wikiId} = useParams();
    const [admin, setadmin] = useState([]);
    const [pseudo, setPseudo] = useState("");
    const [admindata, setadmindata] = useState([]);
    const [errorMessage, setErrorMessage] = useState("");
    const [deleteErrorMessage, setDeleteErrorMessage] = useState("");
    const navigate = useNavigate();

    const handleRetourClick = () => {
        navigate(-1);
    };

    useEffect(() => {
        getEntree();
    },[wikiId]);

    useEffect(()=>{
        if (admin && admin[0]) {
            setadmindata(admin[0].adminsdata);
            console.log(admin.adminsdata);
        }
    },[admin]);


    const getEntree = () => {
        axios.get(`${API_URL}/wiki/${wikiId}/admin`).then((res) => {
            setadmin(res.data);
            console.log(res.data);
        });
    };

    const addToAdmin = (pseudo) => {
        if (pseudo.trim().length === 0) {
            setErrorMessage("Veuillez insérer un pseudo");
        } else {
            setErrorMessage("");
            const message ={
                pseudo: pseudo
            }
            axios.put(API_URL + `/wiki/${wikiId}/admin/add`,message)
            .then((response) => {
                if(response.status === 200){
                    const errorElement = document.getElementById("adminadderror");
                    errorElement.innerHTML ="";
                    navigate(0);
                }
            })
            .catch((error)=>{
                if (error.response.status) {
                    const errorElement = document.getElementById("adminadderror");
                    errorElement.innerHTML ="Erreur lors de l'ajout de l'utilisateur au fichier administateurs";
                }
            });
        }
    };
    const handleSupprAdmin = (index,id) => {
        const message = {
            pseudo: id
        }
        axios.put(API_URL + `/wiki/${wikiId}/admin/delete`,message)
        .then((response) => {
            if(response.status === 200){
                const errorElement = document.getElementById("admindeleteerror");
                errorElement.innerHTML ="";
                navigate(0);
            }
        }).catch((error)=>{
            if (error.response.status){   
                const errorElement = document.getElementById("admindeleteerror");
                errorElement.innerHTML ="Erreur lors de la suppression";
            }
        });
    };

    const majPseudo = (e) => {
        setPseudo(e.target.value);
    };

    return (
        <div className="flex-down">
            {admin && admin[0] && (
                <>
                    <h2>Gestion des administrateurs :</h2>
                    <label>
                        Ajouter un adinistrateur :
                        <input type="text" placeholder='pseudo' onChange={(e) => majPseudo(e)}/>
                    </label>
                    <button onClick={()=>addToAdmin(pseudo)}> Ajouter l'utilisateur aux administateurs</button>
                    <p id="adminadderror">{errorMessage}</p>
                    <div id="ici">
                        <h3>Administateurs Actuels :</h3>
                        {admin && admin.map(function (donnee, index) {
                            return (
                                <div key={index} class="small-box-content flex-down">
                                        <div class="flex-spaced">
                                            <p>{donnee.adminsdata.pseudo}</p>
                                            {donnee.adminsdata._id != localStorage.getItem('account') ? (
                                                <button class="float-right" onClick={() => handleSupprAdmin(index,donnee.adminsdata.pseudo)}>Supprimer les droits</button>
                                            ) : null}
                                        </div>
                                </div>
                            );
                        })}
                        <p id="admindeleteerror"></p>
                    </div>
                    <br/>
                    <button onClick={handleRetourClick}>Retour</button>
                </>
            )}
        </div>
    );
    
}

export default AdminWiki;