const userFetchService = {
    head: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        'Referer': null
    },
    fetch: async (url, options = {}) => {
        try {
            const response = await fetch(url, { headers: userFetchService.head, ...options });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Fetch error: ', error);
        }
    },
    findAllUsers: async () => await userFetchService.fetch('api/users'),
    findUser: async (id) => await userFetchService.fetch(`api/users/${id}`),
    getCurrentUser: async () => await userFetchService.fetch('api/users/current-user'),
    saveNewUser: async (user) => await userFetchService.fetch('api/users', {
        method: 'POST',
        body: JSON.stringify(user)
    }),
    updateUser: async (user) => await userFetchService.fetch('api/users', {
        method: 'PUT',
        body: JSON.stringify(user)
    }),
    deleteUser: async (id) => await userFetchService.fetch(`api/users/${id}`, { method: 'DELETE' })
};

async function loadInitialData() {
    await getTableWithUsers();
    await currentUserNavInfo();
    await getUserInfo()
    setupNewUserButton();
}

async function currentUserNavInfo() {
    const usernameSpan = document.getElementById('currentUserUsername');
    const rolesSpan = document.getElementById('currentUserRoles');
    const currentUser = await userFetchService.getCurrentUser();
    usernameSpan.textContent = currentUser.username;
    rolesSpan.textContent = getUserRoles(currentUser).join(', ');
    const adminTab = document.getElementById('adminTab');
    const adminContent = document.getElementById("admin");
    const userTab = document.getElementById('userTab')
    const userContent = document.getElementById("user");
    if (!getUserRoles(currentUser).includes('ADMIN')) {
        adminTab.style.display = 'none';
        adminContent.style.display = "none";
        userTab.setAttribute("class", "nav-link active")
        userContent.setAttribute("class", "tab-pane show active position-absolute")
        userContent.setAttribute("style", "background-color: #f2f2f2; width: 1185px; height: 750px; margin: 40px 0px")

    }
}

async function getTableWithUsers() {
    const table = $('#mainTableWithUsers tbody');
    table.empty();
    const users = await userFetchService.findAllUsers();
    users.forEach(user => {
        const row = createTableRow(user);
        table.append(row);
    });
}
async function getUserInfo() {
    const table = $('#userInfo tbody');
    const user = await userFetchService.getCurrentUser();
    const roles = getUserRoles(user).join(', ');
    const row = `
    <tr>
        <td>${user.id}</td>
        <td>${user.email}</td>
        <td>${user.username}</td>
        <td>${user.age}</td>
        <td>${roles}</td>
    </tr>`;
    table.append(row);
}

function createTableRow(user) {
    const roles = getUserRoles(user).join(', ');
    return `
    <tr>
        <td>${user.id}</td>
        <td>${user.email}</td>
        <td>${user.username}</td>
        <td>${user.age}</td>
        <td>${roles}</td>
        <td>
            <button type="button" class="btn btn-primary edit-btn" data-userid="${user.id}" data-action="edit" data-toggle="modal" data-target="#userModal">Edit</button>
        </td>
        <td>
            <button type="button" class="btn btn-danger delete-btn" data-userid="${user.id}" data-action="delete" data-toggle="modal" data-target="#userModal">Delete</button>
        </td>
    </tr>`;
}

function getUserRoles(user) {
    return user.roles.map(role => role.name.substring(5));
}

document.addEventListener("DOMContentLoaded", async () => {
    await loadInitialData();

    const userTable = document.getElementById("mainTableWithUsers");
    const saveChangesButton = document.getElementById("saveChangesButton");

    userTable.addEventListener("click", handleTableButtonClick);
    saveChangesButton.addEventListener("click", handleSaveChangesClick);
});

async function handleTableButtonClick(event) {
    const button = event.target.closest("button");
    if (!button) return;

    const userId = button.getAttribute("data-userid");
    const action = button.getAttribute("data-action");

    if (action === "edit") {
        await openModalForEdit(userId);
    } else if (action === "delete") {
        await openModalForDelete(userId);
    }
}

async function openModalForEdit(userId) {
    setModalState('edit');
    const user = await userFetchService.findUser(userId);
    fillModalFields(user);
}

async function openModalForDelete(userId) {
    setModalState('delete');
    const user = await userFetchService.findUser(userId);
    fillModalFields(user);
}

function setModalState(state) {
    const saveChangesButton = document.getElementById("saveChangesButton");
    const fieldset = document.getElementById("fieldset");

    if (state === 'edit') {
        fieldset.disabled = false;
        saveChangesButton.textContent = 'Save Changes';
        saveChangesButton.style.backgroundColor = '#0a6cff';
    } else if (state === 'delete') {
        fieldset.disabled = true;
        saveChangesButton.textContent = 'Delete';
        saveChangesButton.style.backgroundColor = '#e83131';
    }
}

function fillModalFields(user) {
    document.getElementById("user-id").value = user.id;
    document.getElementById("user-name").value = user.username;
    document.getElementById("user-password").value = user.password;
    document.getElementById("user-email").value = user.email;
    document.getElementById("user-age").value = user.age;
    document.getElementById("ed_role").value = getUserRoles(user).includes("ADMIN") ? "admin" : "user";
}

async function handleSaveChangesClick() {
    const userId = document.getElementById("user-id").value;
    const action = document.getElementById("saveChangesButton").textContent;

    if (action === 'Delete') {
        await userFetchService.deleteUser(userId);
    } else {
        const user = getModalUserData();
        await userFetchService.updateUser(user);
    }

    await getTableWithUsers();
}

function getModalUserData() {
    const userId = document.getElementById("user-id").value;
    const username = document.getElementById("user-name").value;
    const password = document.getElementById("user-password").value;
    const email = document.getElementById("user-email").value;
    const age = document.getElementById("user-age").value;
    const selectedRole = document.getElementById("ed_role").value;
    const roles = selectedRole === "admin"
        ? [{ id: 0, name: "ROLE_ADMIN" }, { id: 1, name: "ROLE_USER" }]
        : [{ id: 1, name: "ROLE_USER" }];

    return { id: userId, username, password, email, age, roles };
}

function setupNewUserButton() {
    $('#new-user-btn').click(async () => {
        const user = getNewUserData();
        const response = await userFetchService.saveNewUser(user);
        if (response && response.success) {
            console.log('Пользователь успешно добавлен!');
        } else {
            console.error(response ? response.message : 'Error adding user');
        }
        await getTableWithUsers();
    });
}

function getNewUserData() {
    const addUserForm = $('#user-form');
    const username = addUserForm.find('#username').val().trim();
    const password = addUserForm.find('#password').val().trim();
    const email = addUserForm.find('#email').val().trim();
    const age = addUserForm.find('#age').val().trim();
    const selectedRole = document.getElementById('roles').value;
    const roles = selectedRole === "ADMIN"
        ? [{ id: 0, name: "ROLE_ADMIN" }, { id: 1, name: "ROLE_USER" }]
        : [{ id: 1, name: "ROLE_USER" }];

    return { username, password, email, age, roles };
}